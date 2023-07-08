package llvmir.pass;

import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.Instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Analyzer {
    /**
     * 基本块的支配关系树，键为基本块，值为基本块所支配的所有节点（含自身），键支配值
     */
    private final Map<BasicBlock, Set<BasicBlock>> dominateTree = new HashMap<>();

    /**
     * 基本块的支配关系树，键为基本块，值为基本块所支配的所有节点（含自身），值支配键
     */
    private final Map<BasicBlock, Set<BasicBlock>> dominatedTree = new HashMap<>();

    /**
     * 基本块在支配树中的深度，键为基本块，值为基本块在支配树中的深度
     */
    private final Map<BasicBlock, Integer> depth = new HashMap<>();

    /**
     * 基本块的直接支配者，键为基本块，值为基本块直接支配的其他基本块，键支配值
     */
    private final Map<BasicBlock, Set<BasicBlock>> iDominateTree = new HashMap<>();

    /**
     * 基本块的直接支配者，键为基本块，值为基本块的直接支配者，值直接支配键
     */
    private final Map<BasicBlock, BasicBlock> iDominatedTree = new HashMap<>();

    /**
     * 基本块的支配边界，键为基本块，值为基本块的支配边界
     */
    private final Map<BasicBlock, Set<BasicBlock>> domFrontier = new HashMap<>();

    /**
     * 各基本块的活跃变量，键为基本块，值为基本块的活跃进入变量
     */
    private final Map<BasicBlock, Set<Value>> liveIn = new HashMap<>();

    /**
     * 各基本块的活跃变量，键为基本块，值为基本块的活跃出去变量
     */
    private final Map<BasicBlock, Set<Value>> liveOut = new HashMap<>();

    /**
     * 深度优先搜索，用于构建基本块的支配关系树，当基本块 A 不能不经过 B 而搜索到时，B 严格支配 A。
     * @param cur 当前搜索的基本块
     * @param stop 搜索不经过的基本块
     * @param canReach 已经搜索过的基本块（可以不经过 stop 基本块访问到）
     */
    private void domTreeDFS(BasicBlock cur, BasicBlock stop, Set<BasicBlock> canReach) {
        // 如果当前基本块不是第一次访问到，一定搜索过其底层，直接返回
        if (canReach.contains(cur)) {
            return;
        }
        // 如果当前基本块第一次访问到，添加到 canReach 中
        canReach.add(cur);
        // 如果当前基本块不是 stop 基本块，对其底层进行搜索
        if (cur != stop) {
            for (BasicBlock succ : cur.getTos()) {
                domTreeDFS(succ, stop, canReach);
            }
        }
    }

    private void domTreeAddDepth(BasicBlock cur, int depth) {
        this.depth.put(cur, depth);
        for (BasicBlock succ : iDominateTree.get(cur)) {
            domTreeAddDepth(succ, depth + 1);
        }
    }

    /**
     * 构建函数内所有基本块的严格支配关系树
     * @param func 函数
     */
    private void analyzeDominateAndLive(Function func) {
        // 清空所有容器
        dominateTree.clear();
        dominatedTree.clear();
        iDominateTree.clear();
        iDominatedTree.clear();
        domFrontier.clear();
        liveIn.clear();
        liveOut.clear();

        // 使用深度优先遍历创建dom树
        BasicBlock entrance = func.getBlocks().get(0);
        // 以下算法的正确性需要保证每个基本块均可达
        func.trimBlocks();
        // 对于每个基本块，找到其严格支配的所有基本块
        for (BasicBlock dominator : func.getBlocks()) {
            // 计算可以不经过 dominator 基本块访问到的基本块
            Set<BasicBlock> canReach = new HashSet<>();
            domTreeDFS(entrance, dominator, canReach);
            // 其余必须经过 dominator 基本块访问到的基本块即为 dominator 严格支配的基本块
            for (BasicBlock block : func.getBlocks()) {
                if (!canReach.contains(block)) {
                    dominateTree.putIfAbsent(dominator, new HashSet<>());
                    dominateTree.get(dominator).add(block);
                    dominatedTree.putIfAbsent(block, new HashSet<>());
                    dominatedTree.get(block).add(dominator);
                }
            }

            dominateTree.putIfAbsent(dominator, new HashSet<>());
            dominateTree.get(dominator).add(dominator);
            dominatedTree.putIfAbsent(dominator, new HashSet<>());
            dominatedTree.get(dominator).add(dominator);
        }

        // 对于每个基本块，找到其直接支配者
        for (BasicBlock block : func.getBlocks()) {
            // 如果当前基本块是入口基本块，直接跳过
            iDominateTree.putIfAbsent(block, new HashSet<>());
            if (block != entrance) {
                // 获得当前基本块的所有严格支配者
                Set<BasicBlock> blockStrictDominate = new HashSet<>(dominatedTree.get(block));
                blockStrictDominate.remove(block);
                // 支配集合 等于 当前基本块严格支配集合 的基本块是当前基本块的直接支配者
                for (BasicBlock dominator : dominatedTree.get(block)) {
                    if (dominatedTree.get(dominator).equals(blockStrictDominate)) {
                        if (iDominatedTree.get(block) == null) {
                            iDominatedTree.put(block, dominator);
                            iDominateTree.putIfAbsent(dominator, new HashSet<>());
                            iDominateTree.get(dominator).add(block);
                        } else {
                            throw new RuntimeException("multiple idom");
                        }
                    }
                }
                if (iDominatedTree.get(block) == null) {
                    throw new RuntimeException("no idom");
                }
            }
        }

        // 对于每个基本块，找到其支配边界
        for (BasicBlock a : func.getBlocks()) {
            domFrontier.putIfAbsent(a, new HashSet<>());
            for (BasicBlock b : a.getTos()) {
                BasicBlock x = a;
                // 当 x 不严格支配 b （x 不支配 b 或 x 等于 b）
                while (!dominateTree.get(x).contains(b) || x == b) {
                    domFrontier.putIfAbsent(x, new HashSet<>());
                    domFrontier.get(x).add(b);
                    if (x == entrance) {
                        break;
                    }
                    x = iDominatedTree.get(x);
                }
            }
        }

        // 计算支配树深度
        domTreeAddDepth(entrance, 0);

        // 使用-定义分析
        Map<BasicBlock, Set<Value>> def = new HashMap<>();
        Map<BasicBlock, Set<Value>> use = new HashMap<>();
        for (BasicBlock block : func.getBlocks()) {
            Set<Value> defSet = new HashSet<>();
            Set<Value> useSet = new HashSet<>();
            Set<Value> analyzed = new HashSet<>();
            for (Instruction inst : block.getAllInstructions()) {
                if (!analyzed.contains(inst)) {
                    analyzed.add(inst);
                    defSet.add(inst);
                }
                inst.getOperands().forEach(op -> {
                    if (!analyzed.contains(op)) {
                        analyzed.add(op);
                        useSet.add(op);
                    }
                });
            }
            def.put(block, defSet);
            use.put(block, useSet);
            liveIn.put(block, new HashSet<>());
            liveOut.put(block, new HashSet<>());
        }

        // 计算活跃变量
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock block : func.getBlocks()) {
                // 当前基本块的出口活跃变量 = 后继基本块的入口活跃变量的并集
                final Set<Value> newLiveOut = new HashSet<>();
                for (BasicBlock succ : block.getTos()) {
                    newLiveOut.addAll(liveIn.get(succ));
                }
                // 当前基本块的入口活跃变量 = 当前基本块的定义集合 + (当前基本块的出口活跃变量 - 当前基本块的使用集合)
                final Set<Value> newLiveIn = new HashSet<>(use.get(block));
                newLiveIn.removeAll(def.get(block));
                newLiveIn.addAll(newLiveOut);
                // 如果入口活跃变量或出口活跃变量发生变化，则继续迭代
                if (!newLiveIn.equals(liveIn.get(block)) || !newLiveOut.equals(liveOut.get(block))) {
                    changed = true;
                    liveIn.put(block, newLiveIn);
                    liveOut.put(block, newLiveOut);
                }
            }
        }
    }

    /*========== 公共接口部分 ==========*/
    public Analyzer(Function func) {
        analyzeDominateAndLive(func);
    }

    /**
     * 基本块的支配关系树，键为基本块，值为基本块所支配的所有节点（含自身），键支配值
     */
    public Map<BasicBlock, Set<BasicBlock>> dominateTree() {
        return dominateTree;
    }

    /**
     * 基本块的支配关系树，键为基本块，值为基本块所支配的所有节点（含自身），值支配键
     */
    public Map<BasicBlock, Set<BasicBlock>> dominatedTree() {
        return dominatedTree;
    }

    /**
     * 基本块在支配关系树中的深度
     */
    public int depth(BasicBlock block) {
        if (depth.get(block) == null) {
            throw new RuntimeException("no depth");
        }
        return depth.get(block);
    }

    /**
     * 基本块的直接支配者，键为基本块，值为基本块直接支配的其他基本块，键支配值
     */
    public Map<BasicBlock, Set<BasicBlock>> iDominateTree() {
        return iDominateTree;
    }

    /**
     * 基本块的直接支配者，键为基本块，值为基本块的直接支配者，值直接支配键
     */
    public Map<BasicBlock, BasicBlock> iDominatedTree() {
        return iDominatedTree;
    }

    /**
     * 基本块的支配边界，键为基本块，值为基本块的支配边界
     */
    public Map<BasicBlock, Set<BasicBlock>> domFrontier() {
        return domFrontier;
    }

    /**
     * 各基本块的活跃变量，键为基本块，值为基本块的活跃进入变量
     */
    public Map<BasicBlock, Set<Value>> liveIn() {
        return liveIn;
    }

    /**
     * 各基本块的活跃变量，键为基本块，值为基本块的活跃出去变量
     */
    public Map<BasicBlock, Set<Value>> liveOut() {
        return liveOut;
    }
}
