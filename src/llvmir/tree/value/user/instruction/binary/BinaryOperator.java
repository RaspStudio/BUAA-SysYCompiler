package llvmir.tree.value.user.instruction.binary;

import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.instruction.NormalInstruction;

import java.util.Arrays;

public abstract class BinaryOperator extends NormalInstruction {

    public BinaryOperator(Type valType, String name, BasicBlock parent, Value leftOperand, Value rightOperand) {
        super(valType, name.isEmpty() ? name : "%" + name + "." + Value.allocId(name),
                parent, Arrays.asList(leftOperand, rightOperand));
    }

    public abstract String getOpcode();

    @Override
    public String toString() {
        return "\t" + (name.length() > 0 ? (name + " = ") : "") + getOpcode() + " " + valType + " "
                + operands.get(0).getName() + ", " + operands.get(1).getName();
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }
}
