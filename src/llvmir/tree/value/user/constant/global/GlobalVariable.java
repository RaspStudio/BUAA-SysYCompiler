package llvmir.tree.value.user.constant.global;

import llvmir.tree.Module;
import llvmir.tree.type.PointerType;
import llvmir.tree.type.Type;
import llvmir.tree.type.Types;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantString;

import java.util.Collections;

public class GlobalVariable extends GlobalObject {
    private final boolean isConst;

    public GlobalVariable(Type valType, String name, Value init, boolean isConst) {
        super(Types.pointer(valType), name, Collections.singletonList(init));
        this.isConst = isConst;
    }

    public GlobalVariable(ConstantString string) {
        super(Types.pointer(string.getType()), "str." + Value.allocId(), Collections.singletonList(string));
        this.isConst = true;
    }

    public boolean isConst() {
        return isConst;
    }

    public Type getDataType() {
        return ((PointerType)(valType)).getDerivedType();
    }

    public Value getData() {
        return operands.get(0);
    }

    @Override
    public Module getParent() {
        return null;
    }

    @Override
    public String toString() {
        return name + " = dso_local " + (isConst ? "constant " : "global ")
                + operands.get(0).toString();
    }
}
