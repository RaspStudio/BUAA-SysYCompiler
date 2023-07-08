package llvmir.tree.value.user.instruction.binary;

import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

public class ICmpInst extends BinaryOperator {
    public enum CmpType { EQ, NE, SGT, SGE, SLT, SLE }

    private final CmpType type;

    public ICmpInst(String name, BasicBlock parent, CmpType type, Value leftOperand, Value rightOperand) {
        super(Types.BOOL, name, parent, leftOperand, rightOperand);
        this.type = type;
    }

    public CmpType getCmpType() {
        return type;
    }

    public String toString() {
        return "\t" + (name.length() > 0 ? (name + " = ") : "") + getOpcode() + " " + operands.get(0).getType() + " "
                + operands.get(0).getName() + ", " + operands.get(1).getName();
    }

    @Override
    public String getOpcode() {
        return "icmp " + type.toString().toLowerCase();
    }
}
