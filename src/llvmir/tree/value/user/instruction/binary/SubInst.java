package llvmir.tree.value.user.instruction.binary;

import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

public class SubInst extends BinaryOperator {

    public SubInst(Type valType, String name, BasicBlock parent, Value leftOperand, Value rightOperand) {
        super(valType, name, parent, leftOperand, rightOperand);
    }

    @Override
    public String getOpcode() {
        return "sub";
    }
}
