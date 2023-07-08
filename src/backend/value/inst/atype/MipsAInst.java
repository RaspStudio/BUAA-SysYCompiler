package backend.value.inst.atype;

import backend.value.MipsBlock;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsReg;
import backend.value.inst.MipsInst;

public abstract class MipsAInst extends MipsInst {
    protected MipsReg reg;
    protected MipsAddr addr;

    protected MipsAInst(MipsBlock parent, MipsReg reg, MipsAddr addr, boolean isLoad) {
        super(parent);
        this.reg = reg;
        this.addr = addr;
        if (isLoad) {
            reg.setDef(this);
        } else {
            reg.addUser(this);
        }
        if (addr.getBase() != null) {
            addr.getBase().addUser(this);
        }
    }

    @Override
    public final void replaceUse(MipsReg old, MipsReg newValue) {
        if (reg == old) {
            reg = newValue;
        } else if (addr.getBase() == old) {
            addr = addr.replaceBase(newValue);
        } else {
            throw new RuntimeException("No such use");
        }
    }

    protected abstract String getOpcode();

    @Override
    public final String toString() {
        return (getOpcode() + "    ").substring(0, 4) + "\t\t" + reg + ", " + addr;
    }
}
