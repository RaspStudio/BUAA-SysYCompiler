package llvmir.tree.value.user.instruction.terminator;

import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.instruction.Instruction;

import java.util.List;

public abstract class TerminateInstruction extends Instruction {

    protected TerminateInstruction(Type valType, String name, BasicBlock parent, List<Value> operands) {
        super(valType, name, parent, operands);
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }
}
