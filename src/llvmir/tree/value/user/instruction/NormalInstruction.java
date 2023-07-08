package llvmir.tree.value.user.instruction;

import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.List;

public abstract class NormalInstruction extends Instruction {
    protected NormalInstruction(Type valType, String name, BasicBlock parent, List<Value> operands) {
        super(valType, name, parent, operands);
    }
}
