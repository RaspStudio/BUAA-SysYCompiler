package llvmir.tree.value.user.instruction;

import llvmir.tree.type.PointerType;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.Collections;

public class LoadInst extends NormalInstruction {

    public LoadInst(BasicBlock parent, Value from) {
        super(
                ((PointerType)(from.getType())).getDerivedType(),
                "%load." + Value.allocId("LOAD"), parent,
                Collections.singletonList(from)
        );
    }

    @Override
    public String toString() {
        return "\t" + name + " = load " + valType + ", " + operands.get(0).getType() + " " + operands.get(0).getName();
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }
}
