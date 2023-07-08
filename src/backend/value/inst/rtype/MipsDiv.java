package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;

public class MipsDiv extends MipsRInst {

    public MipsDiv(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop) {
        super(parent, dest, lop, rop);
    }

    public MipsDiv(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm) {
        super(parent, dest, lop, rimm);
    }

    @Override
    protected String getOpcode() {
        return "div";
    }
}
