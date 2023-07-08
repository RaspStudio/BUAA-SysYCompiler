package backend.regalloc;

import util.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RegAllocator<I extends VInst<R>, R extends VReg,
        B extends VBlock<I, R, B>, F extends VFunc<I, R, B>> {

    protected final List<F> functions;
    protected final List<Integer> validColors;

    public RegAllocator(List<F> functions, List<Integer> validColors) {
        this.functions = listOf(functions);
        this.validColors = listOf(validColors);
    }

    public final void allocate() {
        for (F function : functions) {
            allocate(function);
        }
    }

    protected abstract void allocate(F function);

    protected abstract void spill(F function, R reg);

    protected final Graph<R> buildConflictGraph(F function) {
        final Map<B, Set<R>> blockUses = newMap();
        final Map<B, Set<R>> blockDefs = newMap();
        final Map<B, Set<R>> blockLiveIn = newMap();
        final Map<B, Set<R>> blockLiveOut = newMap();

        // 计算每个基本块的使用和定义
        for (B block : function.getBlocks()) {
            final Set<R> analyzed = newSet();
            final Set<R> uses = newSet();
            final Set<R> defs = newSet();
            // 计算每个指令的使用和定义
            block.getInsts().forEach(i -> i.analyze(analyzed, defs, uses));
            blockUses.put(block, uses);
            blockDefs.put(block, defs);
            blockLiveIn.put(block, newSet());
            blockLiveOut.put(block, newSet());
        }

        // 计算每个基本块的活跃变量
        boolean changed = true;
        while (changed) {
            changed = false;
            for (B block : function.getBlocks()) {
                // 当前基本块的出口活跃变量 = 后继基本块的入口活跃变量的并集
                final Set<R> newLiveOut = newSet();
                for (B succ : block.getSuccs()) {
                    newLiveOut.addAll(blockLiveIn.get(succ));
                }
                // 当前基本块的入口活跃变量 = 当前基本块的定义集合 + (当前基本块的出口活跃变量 - 当前基本块的使用集合)
                final Set<R> newLiveIn = setOf(newLiveOut);
                newLiveIn.removeAll(blockDefs.get(block));
                newLiveIn.addAll(blockUses.get(block));
                // 如果入口活跃变量或出口活跃变量发生变化，则继续迭代
                if (!newLiveIn.equals(blockLiveIn.get(block)) || !newLiveOut.equals(blockLiveOut.get(block))) {
                    changed = true;
                    blockLiveIn.put(block, newLiveIn);
                    blockLiveOut.put(block, newLiveOut);
                }
            }
        }

        // 构建冲突图
        final Graph<R> graph = new Graph<>(false);
        for (B block : function.getBlocks()) {
            Set<R> liveOut = blockLiveOut.get(block);
            Map<R, Integer> regStartTime = newMap();
            // 倒序分析每个指令的冲突
            for (int i = block.getInsts().size() - 1; i >= 0; i--) {
                I inst = block.getInsts().get(i);
                // 指令定义的变量：和当前活跃的变量产生冲突，并结束其生命周期
                for (R def : inst.getDefs()) {
                    graph.addNodeIfAbsent(def);
                    // 使用独占寄存器的变量不产生冲突也不考虑活跃性（如栈寄存器）
                    if (def.needAnalyze()) {
                        liveOut.remove(def);
                        graph.setLiveTime(def, regStartTime.getOrDefault(def, block.getInsts().size()) - i);
                        for (R live : liveOut) {
                            graph.addEdgeAndNodeIfAbsent(def, live, 1);
                        }
                    }
                }
                // 指令使用的变量：开始其生命周期
                for (R use : inst.getUses()) {
                    // 使用独占寄存器的变量不产生冲突也不考虑活跃性（如栈寄存器）
                    graph.addNodeIfAbsent(use);
                    if (use.needAnalyze()) {
                        liveOut.add(use);
                        regStartTime.put(use, i);
                    }
                }
            }
            liveOut.forEach(o -> graph.setLiveTime(o, regStartTime.getOrDefault(o, 1 + block.getInsts().size())));
        }

        return graph;
    }

    /* 寄存器分配使用的列表元素 */
    public static <T> List<T> listOf(Collection<T> functions) {
        return new ArrayList<>(functions);
    }

    public static <T> List<T> newList() {
        return listOf(Collections.emptyList());
    }

    /* 寄存器分配使用的映射元素 */
    public static <K, V> Map<K, V> mapOf(Map<K, V> map) {
        return new LinkedHashMap<>(map);
    }

    public static <K, V> Map<K, V> newMap() {
        return mapOf(Collections.emptyMap());
    }

    /* 寄存器分配使用的集合元素 */
    public static <T> Set<T> setOf(Set<T> set) {
        return new LinkedHashSet<>(set);
    }

    public static <T> Set<T> newSet() {
        return setOf(Collections.emptySet());
    }
}
