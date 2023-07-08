package llvmir.pass.gvn;

import llvmir.pass.Analyzer;
import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.User;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.*;
import llvmir.tree.value.user.instruction.terminator.TerminateInstruction;

import java.util.*;

public class GlobalCodeMove extends Pass {

    private Set<Instruction> getPinned(Function function) {
        Set<Instruction> pinned = new HashSet<>();
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction inst : block.getAllInstructions()) {
                if (inst instanceof CallInst || inst instanceof LoadInst || inst instanceof StoreInst ||
                        inst instanceof PhiInst || inst instanceof TerminateInstruction) {
                    pinned.add(inst);
                }
            }
        }
        return pinned;
    }

    private BasicBlock findLCA(BasicBlock a, BasicBlock b, Analyzer analyzer) {
        if (a == null) {
            return b;
        }
        while (analyzer.depth(a) > analyzer.depth(b)) {
            a = analyzer.iDominatedTree().get(a);
        }
        while (analyzer.depth(b) > analyzer.depth(a)) {
            b = analyzer.iDominatedTree().get(b);
        }
        while (a != b) {
            a = analyzer.iDominatedTree().get(a);
            b = analyzer.iDominatedTree().get(b);
        }
        return a;
    }

    private Instruction findInsertBefore(Instruction inst, BasicBlock block, Analyzer analyzer) {
        Instruction insertBefore = null;
        for (Instruction i : block.getAllInstructions()) {
            if (inst.getUsers().contains(i) && !(i instanceof PhiInst)) {
                insertBefore = i;
                break;
            }
        }
        return insertBefore == null ? block.getTerminator() : insertBefore;
    }

    private void realAnalyzeEarliest(Instruction inst, Set<Instruction> analyzed,
                                     Map<Instruction, BasicBlock> earliest, Analyzer analyzer) {
        if (!analyzed.add(inst)) {
            throw new RuntimeException("Already analyzed");
        }
        // 首次进行分析，将当前指令的最先位置设置为当前指令所在的基本块
        earliest.put(inst, inst.getParent().getParent().getBlocks().get(0));
        for (Value o : inst.getOperands()) {
            if (o instanceof Instruction) {
                if (!analyzed.contains(o)) {
                    // 如果操作数还没有分析过，说明上一步出问题了
                    throw new RuntimeException("Not analyzed");
                }
                if (analyzer.depth(earliest.get(o)) > analyzer.depth(earliest.get(inst))) {
                    // 如果操作数的定义基本块更靠后
                    earliest.put(inst, earliest.get(o));
                }
            }
        }
    }

    /**
     * 尝试将指令前提，提到支配树更靠前的位置。
     * 指令的所有操作数一定在指令原位置之前定义，并且一定已经被分析过。
     * @param inst 尝试提前的指令
     * @param analyzed 已分析的指令
     */
    private void analyzeEarliest(Instruction inst, Set<Instruction> analyzed,
                                 Map<Instruction, BasicBlock> earliest, Analyzer analyzer) {
        // 分析前，首先要保证所有操作数都已经分析过
        final Stack<Instruction> toAnalyze = new Stack<>();
        toAnalyze.push(inst);
        while (!toAnalyze.isEmpty()) {
            // 拿到一个指令，确保其所有操作数都已经分析过
            Instruction i = toAnalyze.peek();
            boolean allAnalyzed = true;
            // 检查所有操作数是否都已经分析过
            for (Value o : i.getOperands()) {
                if (o instanceof Instruction && !analyzed.contains(o)) {
                    toAnalyze.push((Instruction) o);
                    allAnalyzed = false;
                }
            }
            if (allAnalyzed) {
                toAnalyze.pop();
                if (!analyzed.contains(i)) {
                    realAnalyzeEarliest(i, analyzed, earliest, analyzer);
                }
            }
        }
    }

    private void realAnalyzeLatest(Instruction inst, Set<Instruction> analyzed, Map<Instruction, BasicBlock> earliest,
                                   Map<Instruction, BasicBlock> latest, Analyzer analyzer) {
        if (!analyzed.add(inst)) {
            throw new RuntimeException("Already analyzed");
        }
        // 维护所有使用者的最近公共祖先
        BasicBlock lca = null;
        for (User user : inst.getUsers()) {
            if (user instanceof Instruction) {
                Instruction o = (Instruction) user;
                if (!analyzed.contains(user)) {
                    throw new RuntimeException("Not analyzed");
                }
                lca = findLCA(
                        lca,
                        o instanceof PhiInst ? ((PhiInst)o).getPhiParent(inst) : o.getParent(),
                        analyzer
                );
            }
        }
        // 目前已经获得了所有使用者的最近公共祖先，在 LCA 和 最早块之间均可放置当前指令
        BasicBlock best = lca;
        if (best == null) {
            throw new RuntimeException("No user inst!");
        }
        // 在可选的几个块中选择最优的设为当前指令的最晚位置
        while (lca != earliest.get(inst)) {
            // 当前块不是最早块，父块可以放置当前指令，迭代到父块（可能是最早块）
            lca = analyzer.iDominatedTree().get(lca);
            // 选择循环深度低的块
            if (lca.loopDepth() < best.loopDepth()) {
                best = lca;
            }
        }
        latest.put(inst, best);

        // 如果当前指令的位置已经不在最早和最晚位置之间，将当前指令移动到最晚位置
        if (latest.get(inst) != inst.getParent()) {
            inst.getParent().moveInst(inst, findInsertBefore(inst, latest.get(inst), analyzer));
            setChanged();
        }
    }

    private void analyzeLatest(Instruction inst, Set<Instruction> analyzed, Map<Instruction, BasicBlock> earliest,
                               Map<Instruction, BasicBlock> latest, Analyzer analyzer) {
        // 分析前，首先要保证所有操作数都已经分析过
        final Stack<Instruction> toAnalyze = new Stack<>();
        toAnalyze.push(inst);
        while (!toAnalyze.isEmpty()) {
            // 拿到一个指令，确保其所有操作数都已经分析过
            Instruction i = toAnalyze.peek();
            boolean allAnalyzed = true;
            // 检查所有操作数是否都已经分析过
            for (Value o : i.getUsers()) {
                if (o instanceof Instruction && !analyzed.contains(o)) {
                    toAnalyze.push((Instruction) o);
                    allAnalyzed = false;
                }
            }
            if (allAnalyzed) {
                toAnalyze.pop();
                if (!analyzed.contains(i)) {
                    realAnalyzeLatest(i, analyzed, earliest, latest, analyzer);
                }
            }
        }
    }

    private void runFuncGCM(Function function) {
        // 统计不可移动指令
        final Set<Instruction> pinned = getPinned(function);
        // 初始化容器
        final Analyzer analyzer = new Analyzer(function);


        // 分析指令最先位置
        final Map<Instruction, BasicBlock> earliest = new HashMap<>();
        final Set<Instruction> analyzedEarliest = new HashSet<>(pinned);
        pinned.forEach(inst -> earliest.put(inst, inst.getParent()));

        for (BasicBlock block : function.getBlocks()) {
            for (Instruction inst : block.getAllInstructions()) {
                if (!analyzedEarliest.contains(inst)) {
                    // 没有分析过的可移动指令
                    analyzeEarliest(inst, analyzedEarliest, earliest, analyzer);
                } else if (pinned.contains(inst)) {
                    // 不可移动指令
                    inst.getOperands().stream().
                            filter(o -> o instanceof Instruction && !analyzedEarliest.contains(o)).
                            forEach(o -> analyzeEarliest((Instruction) o, analyzedEarliest, earliest, analyzer));
                }
            }
        }

        // 分析指令最晚位置
        final Map<Instruction, BasicBlock> latest = new HashMap<>();
        final Set<Instruction> analyzedLatest = new HashSet<>(pinned);
        // 先分析不可移动指令的操作数，因为不会再分析这些指令了
        pinned.forEach(inst -> latest.put(inst, inst.getParent()));

        // 分析可移动指令的最晚位置
        for (int i = function.getBlocks().size() - 1; i >= 0; i--) {
            BasicBlock block = function.getBlocks().get(i);
            for (int j = block.getAllInstructions().size() - 1; j >= 0; j--) {
                Instruction inst = block.getAllInstructions().get(j);
                if (!analyzedLatest.contains(inst)) {
                    // 没有分析过的可移动指令
                    analyzeLatest(inst, analyzedLatest, earliest, latest, analyzer);
                } else if (pinned.contains(inst)) {
                    // 不可移动指令
                    inst.getUsers().stream()
                            .filter(o -> o instanceof Instruction && !analyzedLatest.contains(o))
                            .forEach(user -> analyzeLatest((Instruction) user, analyzedLatest, earliest, latest, analyzer));
                }
            }
        }

        // 移动指令
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction inst : block.getAllInstructions()) {
                if (latest.get(inst) != inst.getParent()) {
                    throw new RuntimeException("Not moved In analyzeLatest!");
                }
            }
        }
    }

    @Override
    protected void pass(Module module) {
        module.getFunctions().forEach(this::runFuncGCM);
    }
}
