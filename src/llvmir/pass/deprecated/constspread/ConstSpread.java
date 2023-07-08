package llvmir.pass.deprecated.constspread;

import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.global.GlobalVariable;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.binary.*;

public class ConstSpread extends Pass {

    public static void constSpread(Instruction inst) {
        if (Instruction.isArith(inst.getClass()) &&
                inst.getOperands().stream().allMatch(o -> o instanceof ConstantInt)) {
            // 所有参数都是常数，可以进行常数传播
            ConstantInt result;
            int lop = ((ConstantInt) inst.getOperands().get(0)).getValue();
            int rop = ((ConstantInt) inst.getOperands().get(1)).getValue();
            if (inst instanceof AddInst) {
                // 加法
                result = new ConstantInt(lop + rop);
            } else if (inst instanceof SubInst) {
                // 减法
                result = new ConstantInt(lop - rop);
            } else if (inst instanceof MulInst) {
                // 乘法
                result = new ConstantInt(lop * rop);
            } else if (inst instanceof SDivInst) {
                // 除法
                result = new ConstantInt(lop / rop);
            } else if (inst instanceof SRemInst) {
                // 取余
                result = new ConstantInt(lop % rop);
            } else {
                // 未知指令
                throw new RuntimeException("Unknown Instruction");
            }
            // 替换所有使用
            inst.replaceSelfWith(result);
            inst.getParent().removeInst(inst);
        } else {
            // 不能进行常数传播
            throw new RuntimeException("Cannot const spread");
        }
    }

    private void spread(BasicBlock block) {
        for (GlobalVariable var : block.getParent().getParent().getVariables()) {
            if (var.isConst() && var.getData() instanceof ConstantInt) {
                var.replaceSelfWith(new ConstantInt(((ConstantInt) var.getData()).getValue()));
            }
        }
        for (Instruction inst : block.getAllInstructions()) {
            // 检查是否所有参数都是常数
            if (Instruction.isArith(inst.getClass()) &&
                    inst.getOperands().stream().allMatch(o -> o instanceof ConstantInt)) {
                // 所有参数都是常数，可以进行常数传播
                constSpread(inst);
                setChanged();
            }
        }
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(f -> f.getBlocks().forEach(this::spread));
    }
}
