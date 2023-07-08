package llvmir.pass.constspread;

import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.binary.AddInst;
import llvmir.tree.value.user.instruction.binary.MulInst;
import llvmir.tree.value.user.instruction.binary.SDivInst;
import llvmir.tree.value.user.instruction.binary.SRemInst;
import llvmir.tree.value.user.instruction.binary.SubInst;

public class ConstSpread extends Pass {

    private void spread(BasicBlock block) {
        for (Instruction inst : block.getAllInstructions()) {
            // 检查是否所有参数都是常数
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
                block.removeInst(inst);
                setChanged();
            }
        }
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(f -> f.getBlocks().forEach(this::spread));
    }
}
