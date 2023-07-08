package llvmir.tree.value;

import llvmir.tree.type.Type;
import llvmir.tree.value.user.instruction.AllocaInst;

public class Argument extends Value {
    private final int argNo;
    private AllocaInst allocedSpace = null;

    public Argument(Type valType, String name, int argNo) {
        super(valType,"%" + name);
        this.argNo = argNo;
    }

    public int getArgNo() {
        return argNo;
    }

    public void setAlloca(AllocaInst var) {
        if (allocedSpace == null) {
            allocedSpace = var;
        } else {
            throw new RuntimeException("Cannot Set Alloca Twice");
        }
    }

    public AllocaInst getAlloca() {
        return allocedSpace;
    }

    @Override
    public String toString() {
        return valType + " " + name;
    }

}
