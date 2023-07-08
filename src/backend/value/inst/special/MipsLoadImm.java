package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;

import java.util.Collections;
import java.util.List;

public class MipsLoadImm extends MipsInst {
    private final MipsReg dest;
    private final MipsImm imm;

    public MipsLoadImm(MipsBlock parent, MipsReg dest, MipsImm imm) {
        super(parent);
        this.dest = dest;
        this.imm = imm;
        dest.setDef(this);
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.singletonList(dest);
    }

    @Override
    public List<MipsReg> getUses() {
        return Collections.emptyList();
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        throw new RuntimeException("Cannot replace use");
    }

    @Override
    public String toString() {
        return "li\t\t" + dest + ", " + imm;
    }
}
