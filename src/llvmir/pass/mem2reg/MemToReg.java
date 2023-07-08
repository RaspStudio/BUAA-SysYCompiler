package llvmir.pass.mem2reg;

import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.AllocaInst;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.LoadInst;
import llvmir.tree.value.user.instruction.StoreInst;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemToReg extends Pass {
    /**
     * 基本块的严格支配关系树，键为基本块，值为基本块所严格支配的所有节点
     */
    private final Map<BasicBlock, List<BasicBlock>> domTree = new HashMap<>();

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

    /**
     * 构建函数内所有基本块的严格支配关系树
     * @param func 函数
     */
    private void createDomTree(Function func) {
        // 使用深度优先遍历创建dom树
        BasicBlock entrance = func.getBlocks().get(0);
        // 对于每个基本块，找到其严格支配的所有基本块
        for (BasicBlock dominator : func.getBlocks()) {
            // 计算可以不经过 dominator 基本块访问到的基本块
            Set<BasicBlock> canReach = new HashSet<>();
            domTreeDFS(entrance, dominator, canReach);
            // 其余必须经过 dominator 基本块访问到的基本块即为 dominator 严格支配的基本块
            List<BasicBlock> dominated = new ArrayList<>();
            for (BasicBlock block : func.getBlocks()) {
                if (!canReach.contains(block)) {
                    dominated.add(block);
                }
            }
            domTree.put(dominator, dominated);
        }
    }

    /**
     * 检查是否所有 Load 指令的块都被 Store 指令非严格支配
     * @param storeInst Store 指令
     * @param loads Load 指令集合
     * @return 是否所有 Load 指令都被 Store 指令支配
     */
    private boolean allLoadDominatedBy(StoreInst storeInst, List<LoadInst> loads) {
        BasicBlock storeBlock = storeInst.getParent();
        List<BasicBlock> strictDominated = domTree.get(storeBlock);
        for (LoadInst load : loads) {
            BasicBlock loadBlock = load.getParent();
            // 当 store 直接支配或同块中 load 靠后时为合理
            // 反之直接返回不合理
            if (!(strictDominated.contains(loadBlock) || loadBlock == storeBlock)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将给定的 Alloca 指令涉及的变量转化为 Phi 指令
     * @param inst Alloca 指令
     */
    private void allocaToPhi(AllocaInst inst) {
        List<LoadInst> loads = new ArrayList<>();
        List<StoreInst> stores = new ArrayList<>();
        // 分析该 Alloca 指令涉及的变量的定义和使用
        inst.getUsers().forEach(o -> {
            if (o instanceof LoadInst) {
                loads.add((LoadInst) o);
            } else if (o instanceof StoreInst) {
                stores.add((StoreInst) o);
            } else {
                throw new RuntimeException("AllocaInst can only be used by LoadInst or StoreInst");
            }
        });

        // 尝试进行优化或构造 Phi 指令，在分支中处理掉相关的所有 Load/Store 语句，分支结束后消去 Alloca 语句。
        if (loads.size() == 0) {
            // 如果该 Alloca 指令没有被 Load 指令使用，那么直接删除该 Alloca 指令及其相关的 Store 指令
            stores.forEach(o -> o.getParent().removeInst(o));
            setChanged();
        } else if (stores.size() == 1 && allLoadDominatedBy(stores.get(0), loads)) {
            // 该 Alloca 指令只被赋值过一次，可视为只读，将 Store 的内容作为常数传播给所有 Load 指令
            StoreInst store = stores.get(0);
            Value constValue = store.getOperand(0);
            // 删除不必要的 Store
            store.getParent().removeInst(store);
            // 将所有的 Load 指令替换成定值或未定义值
            loads.forEach(o -> {
                o.getParent().removeInst(o);
                o.replaceSelfWith(
                        // Load 和 Store 基本块相同但先于 Store 使用时，替换为未定义值
                        o.getParent() == store.getParent() && o.index() < store.index() ?
                                ConstantInt.ZERO : constValue);
            });
            setChanged();
        } else if (loads.stream().allMatch(o -> o.getParent() == loads.get(0).getParent())
                && stores.stream().allMatch(o -> o.getParent() == loads.get(0).getParent())) {
            // 该变量的所有 Load/Store 在一个基本块中，替换成虚拟寄存器
            Value curValue = ConstantInt.ZERO;
            List<Instruction> loadAndStores = new ArrayList<>();
            loadAndStores.addAll(loads);
            loadAndStores.addAll(stores);
            loadAndStores.sort(Comparator.comparingInt(Instruction::index));
            for (Instruction i : loadAndStores) {
                if (i instanceof LoadInst) {
                    i.replaceSelfWith(curValue);
                    i.getParent().removeInst(i);
                } else if (i instanceof StoreInst) {
                    curValue = i.getOperand(0);
                    i.getParent().removeInst(i);
                } else {
                    throw new RuntimeException("Invalid Instruction In List! [Mem2Reg]");
                }
            }
            setChanged();
        } else {
            // 一般情况，非只读变量，分配的内存被写入多次，需要构造 Phi 节点
            return;// todo 不能优化的情况还没写，暂时先只尝试做优化
        }

        // 该变量处理完毕，消去 Alloca 语句
        inst.getParent().removeInst(inst);
    }

    /**
     * 获取函数中可以被提升的 Alloca 指令
     * @param function 函数
     * @return 可以被提升的 Alloca 指令列表
     */
    private List<AllocaInst> getPromotable(Function function) {
        List<AllocaInst> promotable = new ArrayList<>();
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof AllocaInst && ((AllocaInst) inst).isPromotable()) {
                promotable.add((AllocaInst) inst);
            }
        }));
        return promotable;
    }

    /**
     * 将函数为变量构造的 Alloca 指令转化为 Phi 指令
     * @param function 函数
     */
    private void passFunction(Function function) {
        createDomTree(function);
        getPromotable(function).forEach(this::allocaToPhi);
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(this::passFunction);
    }
}
