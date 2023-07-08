package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsReg;

import java.util.Collections;
import java.util.List;

public class MipsComment extends MipsInst {
    private final String comment;

    public MipsComment(MipsBlock parent, String comment) {
        super(parent);
        this.comment = comment;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "#   " + comment;
    }
}
