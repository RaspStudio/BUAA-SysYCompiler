package llvmir.pass.deadcode;

import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.type.VoidType;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.Instruction;

import java.util.List;

public class DeleteDead extends Pass {

    private boolean deleteDeadInst(BasicBlock block) {
        boolean changed = false;
        List<Instruction> insts = block.getAllInstructions();
        for (int i = insts.size() - 1; i >= 0; i--) {
            Instruction inst = insts.get(i);
            if (!(inst.getType() instanceof VoidType) && inst.getUsers().isEmpty() && !inst.hasSideEffect()) {
                inst.getParent().removeInst(inst);
                changed = true;
            }
        }
        return changed;
    }

    private void deleteDeadInst(Function function) {
        boolean changed;
        do {
            changed = false;
            for (BasicBlock block : function.getBlocks()) {
                changed |= deleteDeadInst(block);
            }
            if (changed) {
                setChanged();
            }
        } while (changed);
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(this::deleteDeadInst);
    }
}
