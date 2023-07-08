package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsReg;

import java.util.Collections;
import java.util.List;

public class MipsMove extends MipsInst {
    private final MipsReg dest;
    private MipsReg src;

    public MipsMove(MipsBlock parent, MipsReg dest, MipsReg src) {
        super(parent);
        this.dest = dest;
        this.src = src;
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.singletonList(dest);
    }

    @Override
    public List<MipsReg> getUses() {
        return Collections.singletonList(src);
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        if (src == old) {
            src = newValue;
        }
    }

    @Override
    public String toString() {
        return "move\t\t" + dest + ", " + src;
    }
}
