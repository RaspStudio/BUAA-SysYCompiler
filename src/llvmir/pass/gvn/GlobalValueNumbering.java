package llvmir.pass.gvn;

import llvmir.pass.Analyzer;
import llvmir.pass.Pass;
import llvmir.pass.deprecated.constspread.ConstSpread;
import llvmir.tree.Module;
import llvmir.tree.type.IntegerType;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.constant.global.GlobalVariable;
import llvmir.tree.value.user.instruction.CallInst;
import llvmir.tree.value.user.instruction.GetElementPtrInst;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.LoadInst;
import llvmir.tree.value.user.instruction.binary.AddInst;
import llvmir.tree.value.user.instruction.binary.BinaryOperator;
import llvmir.tree.value.user.instruction.binary.ICmpInst;
import llvmir.tree.value.user.instruction.binary.MulInst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GlobalValueNumbering extends Pass {

    private boolean canGVN(Instruction inst) {
        if (inst instanceof CallInst && !((CallInst) inst).isBuiltIn()) {
            for (int i = 1; i < inst.getOperands().size(); i++) {
                Value operand = inst.getOperands().get(i);
                if (!(operand.getType() instanceof IntegerType)) {
                    return false;
                }
            }
            for (BasicBlock block : ((Function)(inst.getOperand(0))).getBlocks()) {
                for (Instruction instruction : block.getAllInstructions()) {
                    if (instruction instanceof CallInst) {
                        return false;
                    } else if (instruction.getOperands().stream().noneMatch(o -> o instanceof GlobalVariable)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return Instruction.isArith(inst.getClass()) ||
                    inst instanceof ICmpInst || inst instanceof GetElementPtrInst;
        }
    }

    private void addGVNRef(String key, Instruction inst,
                              HashMap<String, Instruction> gvnValue, HashMap<String, Integer> gvnCount) {
        if (gvnValue.containsKey(key)) {
            gvnCount.merge(key, 1, Integer::sum);
        } else {
            gvnValue.put(key, inst);
            gvnCount.put(key, 1);
        }
    }

    private boolean tryUseExistingGVN(Instruction inst, HashMap<String, Instruction> gvnValue, HashMap<String, Integer> gvnCount) {
        StringBuilder hash = new StringBuilder();
        boolean replaced = false;
        if (inst instanceof CallInst) {
            // 计算 CallInst 的 GVN Hash
            hash.append(inst.getPureName()).append("(");
            for (int i = 1; i < inst.getOperands().size(); i++) {
                hash.append(inst.getOperand(i).getPureName()).append(", ");
            }
            hash.append(")");
        } else if (inst instanceof ICmpInst) {
            // 计算 ICmpInst 的 GVN Hash
            hash.append(((ICmpInst) inst).getOpcode()).append("<");
            hash.append(inst.getOperand(0).getPureName()).append(", ");
            hash.append(inst.getOperand(1).getPureName()).append(">");
        } else if (inst instanceof GetElementPtrInst) {
            // 计算 GetElementPtrInst 的 GVN Hash
            hash.append(inst.getOperand(0).getPureName());
            for (int i = 1; i < inst.getOperands().size(); i++) {
                hash.append("[").append(inst.getOperand(i).getPureName()).append("]");
            }
        } else if (Instruction.isArith(inst.getClass())) {
            // 计算算术指令的 GVN Hash
            List<String> ops = new ArrayList<>();
            inst.getOperands().forEach(o -> ops.add(o.getPureName()));
            if (inst instanceof AddInst || inst instanceof MulInst) {
                ops.sort(String::compareTo);
            }
            hash.append(((BinaryOperator)inst).getOpcode()).append("{").append(String.join(", ", ops)).append("}");
        } else {
            throw new RuntimeException("Unknown GVN-Able Instruction");
        }
        // 尝试查找并替换
        if (gvnValue.containsKey(hash.toString())) {
            inst.replaceSelfWith(gvnValue.get(hash.toString()));
            inst.getParent().removeInst(inst);
            replaced = true;
        }
        // 添加到 GVN 表
        addGVNRef(hash.toString(), inst, gvnValue, gvnCount);
        return replaced;
    }

    private void searchForGVN(BasicBlock cur, Analyzer analyzer,
                              HashMap<String, Instruction> gvnValue, HashMap<String, Integer> gvnCount) {
        // 进行常数传播
        for (GlobalVariable var : cur.getParent().getParent().getVariables()) {
            if (var.isConst() && var.getData() instanceof ConstantInt) {
                var.getUsers().forEach(o -> {
                    if (o instanceof LoadInst) {
                        LoadInst load = (LoadInst) o;
                        load.replaceSelfWith(var.getData());
                        load.getParent().removeInst(load);
                    } else {
                        throw new RuntimeException("Unknown User of Const Global Variable");
                    }
                });
            }
        }
        for (Instruction inst : cur.getAllInstructions()) {
            if (Instruction.isArith(inst.getClass()) &&
                    inst.getOperands().stream().allMatch(o -> o instanceof ConstantInt)) {
                // 所有参数都是常数，可以进行常数传播
                ConstSpread.constSpread(inst);
                setChanged();
            }
        }

        HashMap<String, Instruction> curLayerGvnValue = new HashMap<>(gvnValue);
        HashMap<String, Integer> curLayerGvnCount = new HashMap<>(gvnCount);
        for (Instruction inst : cur.getAllInstructions()) {
            if (canGVN(inst)) {
                if (tryUseExistingGVN(inst, curLayerGvnValue, curLayerGvnCount)) {
                    setChanged();
                }
            }
        }

        for (BasicBlock next : analyzer.iDominateTree().get(cur)) {
            searchForGVN(next, analyzer, curLayerGvnValue, curLayerGvnCount);
        }
    }

    private void runFuncGVN(Function function) {
        final HashMap<String, Instruction> gvnValue = new HashMap<>();
        final HashMap<String, Integer> gvnCount = new HashMap<>();
        final Analyzer analyzer = new Analyzer(function);
        searchForGVN(function.getBlocks().get(0), analyzer, gvnValue, gvnCount);
    }

    @Override
    protected void pass(Module module) {
        module.getFunctions().forEach(this::runFuncGVN);
    }
}
