package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.meta.MipsLabel;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;

import java.util.Collections;

public class MipsJal extends MipsCallInst {
    private final MipsLabel label;

    public MipsJal(MipsBlock parent, MipsLabel label, boolean hasReturn) {
        // todo 在 def 集合中可能有 v0 当返回值
        super(parent,
                hasReturn ? Collections.singletonList(MipsReg.of(MipsRegs.V0)) : Collections.emptyList(),
                Collections.singletonList(MipsReg.of(MipsRegs.RA))
        );
        this.label = label;
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        throw new RuntimeException("Cannot replace use");
    }

    @Override
    public String toString() {
        return "jal " + "\t\t" + label.label();
    }
}
