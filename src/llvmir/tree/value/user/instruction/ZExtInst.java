package llvmir.tree.value.user.instruction;

import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.Collections;

public class ZExtInst extends NormalInstruction {
    public ZExtInst(BasicBlock parent, Value src, Type toType) {
        super(toType, "%ext." + Value.allocId("ZEXT"),
                parent, Collections.singletonList(src));
    }

    @Override
    public String toString() {
        return "\t" + name + " = zext " + operands.get(0).getType() + " " + operands.get(0).getName()
                + " to " + valType;
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }
}
