package backend.value.inst.atype;

import backend.value.MipsBlock;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsReg;

import java.util.Collections;
import java.util.List;

public class MipsLoadWord extends MipsAInst {

    public MipsLoadWord(MipsBlock parent, MipsReg reg, MipsAddr addr) {
        super(parent, reg, addr, true);
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.singletonList(reg);
    }

    @Override
    public List<MipsReg> getUses() {
        return addr.getBase() == null ? Collections.emptyList() : Collections.singletonList(addr.getBase());
    }

    @Override
    protected String getOpcode() {
        return "lw";
    }
}
