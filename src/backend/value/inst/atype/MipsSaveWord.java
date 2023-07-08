package backend.value.inst.atype;

import backend.value.MipsBlock;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsReg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MipsSaveWord extends MipsAInst {

    public MipsSaveWord(MipsBlock parent, MipsReg reg, MipsAddr addr) {
        super(parent, reg, addr, false);
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.emptyList();
    }

    @Override
    public List<MipsReg> getUses() {
        return addr.getBase() == null ? Collections.singletonList(reg) : Arrays.asList(reg, addr.getBase());
    }

    @Override
    protected String getOpcode() {
        return "sw";
    }
}
