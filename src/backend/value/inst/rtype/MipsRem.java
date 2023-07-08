package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;

public class MipsRem extends MipsRInst {

    public MipsRem(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop) {
        super(parent, dest, lop, rop);
    }

    public MipsRem(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm) {
        super(parent, dest, lop, rimm);
    }

    @Override
    protected String getOpcode() {
        return "rem";
    }

}
