package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsReg;

import java.util.Collections;
import java.util.List;

public class MipsJr extends MipsInst {
    private final MipsReg reg;

    public MipsJr(MipsBlock parent, MipsReg reg) {
        super(parent);
        this.reg = reg;
        reg.addUser(this);
    }

    @Override
    public String toString() {
        return "jr  \t\t" + reg;
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.emptyList();
    }

    @Override
    public List<MipsReg> getUses() {
        return Collections.singletonList(reg);
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        throw new RuntimeException("Cannot replace use");
    }
}
