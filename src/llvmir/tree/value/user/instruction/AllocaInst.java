package llvmir.tree.value.user.instruction;

import llvmir.tree.type.ArrayType;
import llvmir.tree.type.PointerType;
import llvmir.tree.type.Type;
import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.Collections;

public class AllocaInst extends NormalInstruction {
    private final Type objectType;

    public AllocaInst(Type allocType, String name, BasicBlock parent) {
        super(Types.pointer(allocType),"%" + name + ".alloc" + Value.allocId("ALLOC"), parent, Collections.emptyList());
        objectType = allocType;
    }

    public Type getElementType() {
        return objectType;
    }

    @Override
    public String toString() {
        return "\t" + name + " = alloca " + ((PointerType)(valType)).getDerivedType();
    }

    public boolean isPromotable() {
        return !(objectType instanceof ArrayType);
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }
}
