package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsLabel;
import backend.value.meta.MipsReg;

import java.util.Collections;
import java.util.List;

public class MipsJump extends MipsInst {
    private final MipsLabel label;

    public MipsJump(MipsBlock parent, MipsLabel label) {
        super(parent);
        this.label = label;
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.emptyList();
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
        return "j   \t\t" + label.label();
    }

}