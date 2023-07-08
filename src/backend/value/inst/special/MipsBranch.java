package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsCmpType;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsLabel;
import backend.value.meta.MipsReg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MipsBranch extends MipsInst {
    private MipsReg lop;
    private MipsReg rop;
    private final MipsCmpType type;
    private final MipsLabel label;

    public MipsBranch(MipsBlock parent, MipsReg lop, MipsReg rop, MipsCmpType type, MipsLabel label) {
        super(parent);
        this.lop = lop;
        this.rop = rop;
        this.type = type;
        this.label = label;
        lop.addUser(this);
        rop.addUser(this);
    }

    @Override
    public List<MipsReg> getDefs() {
        return Collections.emptyList();
    }

    @Override
    public List<MipsReg> getUses() {
        return Arrays.asList(lop, rop);
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        if (lop == old) {
            lop = newValue;
        } else if (rop == old) {
            rop = newValue;
        } else {
            throw new RuntimeException("No such use");
        }
    }

    @Override
    public String toString() {
        return type.getBranch() + "\t\t" + lop  + ", " + rop + ", " + label.label();
    }
}
