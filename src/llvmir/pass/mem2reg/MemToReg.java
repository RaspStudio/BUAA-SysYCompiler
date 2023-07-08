package llvmir.pass.mem2reg;

import llvmir.pass.Analyzer;
import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.*;

import java.util.*;

public class MemToReg extends Pass {
    /**
     * 活跃变量、支配关系分析器
     */
    private Analyzer analyzer;

    /**
     * 检查是否所有 Load 指令的块都被 Store 指令非严格支配
     * @param storeInst Store 指令
     * @param loads Load 指令集合
     * @return 是否所有 Load 指令都被 Store 指令支配
     */
    private boolean allLoadDominatedBy(StoreInst storeInst, List<LoadInst> loads) {
        BasicBlock storeBlock = storeInst.getParent();
        Set<BasicBlock> strictDominated = analyzer.dominateTree().get(storeBlock);
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

    private Value get(Stack<Value> stack) {
        return stack.isEmpty() ? ConstantInt.ZERO : stack.peek();
    }

    /**
     * 消除相关的 Load 和 Store
     */
    private void renameDFS(Stack<Value> valueOfVar, BasicBlock cur,
                           Set<LoadInst> loads, Set<StoreInst> stores, Map<BasicBlock, PhiInst> phiInstMap) {
        Stack<Value> curBlockValueOfVar = new Stack<>();
        for (Value value : valueOfVar) {
            curBlockValueOfVar.push(value);
        }

        // 为当前基本块的每个指令分配新的虚拟寄存器
        for (Instruction inst : cur.getAllInstructions()) {
            if (inst instanceof LoadInst && loads.contains(inst)) {
                // 如果是 Load 指令，则将其替换为虚拟寄存器
                inst.replaceSelfWith(get(curBlockValueOfVar));
                inst.getParent().removeInst(inst);
            } else if (inst instanceof StoreInst && stores.contains(inst)) {
                // 如果是 Store 指令，则将其替换为虚拟寄存器
                curBlockValueOfVar.push(inst.getOperand(0));
                inst.getParent().removeInst(inst);
            } else if (inst == phiInstMap.get(cur)) {
                curBlockValueOfVar.push(inst);
            }
        }

        // 为后继基本块的 phi 指令添加来源
        for (BasicBlock succ : cur.getTos()) {
            if (phiInstMap.containsKey(succ)) {
                phiInstMap.get(succ).addIncoming(get(curBlockValueOfVar), cur);
            }
        }

        // 递归处理后继基本块
        for (BasicBlock succ : analyzer.iDominateTree().get(cur)) {
            renameDFS(curBlockValueOfVar, succ, loads, stores, phiInstMap);
        }
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
            HashSet<BasicBlock> defBlocks = new HashSet<>();
            stores.forEach(o -> defBlocks.add(o.getParent()));
            // 以下两个集合对应算法中的 F 和 W 集合
            HashSet<BasicBlock> phiInserted = new HashSet<>();
            HashSet<BasicBlock> containDefs = new HashSet<>(defBlocks);

            while (!containDefs.isEmpty()) {
                BasicBlock block = containDefs.iterator().next();
                containDefs.remove(block);
                for (BasicBlock frontier : analyzer.domFrontier().get(block)) {
                    if (!phiInserted.contains(frontier)) {
                        phiInserted.add(frontier);
                        if (!defBlocks.contains(frontier)) {
                            containDefs.add(frontier);
                        }
                    }
                }
            }

            // 为每个需要插入 Phi 指令的基本块构造 Phi 指令
            Map<BasicBlock, PhiInst> phiMap = new HashMap<>();
            for (BasicBlock block : phiInserted) {
                // 剪枝：如果该基本块中没有用到为该变量分配的内存，那么不需要插入 Phi 指令
                if (analyzer.liveIn().get(block).contains(inst)) {
                    // 插入 Phi 指令
                    PhiInst phi = new PhiInst(inst.getElementType(), inst.getName() + "_" + block.getPureName(), block);
                    phiMap.put(block, phi);
                    block.insertHead(phi);
                }
            }

            // 进行 Phi 指令重命名
            Stack<Value> valueStack = new Stack<>();
            renameDFS(valueStack, inst.getParent(),
                    Collections.unmodifiableSet(new HashSet<>(loads)),
                    Collections.unmodifiableSet(new HashSet<>(stores)), phiMap);
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
        analyzer = new Analyzer(function);
        getPromotable(function).forEach(this::allocaToPhi);
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(this::passFunction);
    }

}
