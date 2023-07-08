package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;

import java.util.Arrays;
import java.util.Collections;

public class MipsSyscall extends MipsCallInst {
    public MipsSyscall(MipsBlock parent, boolean isInput) {
        super(
                parent,
                isInput ? Collections.singletonList(MipsReg.of(MipsRegs.V0)) :
                        Arrays.asList(MipsReg.of(MipsRegs.A0), MipsReg.of(MipsRegs.V0)),
                isInput ? Collections.singletonList(MipsReg.of(MipsRegs.V0)) :
                        Collections.emptyList()
        );
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        throw new RuntimeException("Cannot replace use");
    }

    @Override
    public String toString() {
        return "syscall";
    }

    public static final int GET_INT = 5;
    public static final int PUT_INT = 1;
    public static final int PUT_STR = 4;
}
