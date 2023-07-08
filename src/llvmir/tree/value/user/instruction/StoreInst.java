package llvmir.tree.value.user.instruction;

import llvmir.tree.type.PointerType;
import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.Arrays;

public class StoreInst extends NormalInstruction {

    public StoreInst(BasicBlock parent, Value val, Value dest) {
        super(Types.VOID, "", parent, Arrays.asList(val, dest));
        if (!(dest.getType() instanceof PointerType)) {
            throw new RuntimeException("Cannot Assign To Non-Addr Value!");
        }
    }

    @Override
    public String toString() {
        return "\tstore " + operands.get(0).getType() + " " + operands.get(0).getName() + ", " +
                operands.get(1).getType() + " " + operands.get(1).getName();
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}
