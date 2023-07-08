package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.inst.MipsCmpType;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;

public class MipsCmp extends MipsRInst {
    protected final MipsCmpType type;

    public MipsCmp(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop, MipsCmpType type) {
        super(parent, dest, lop, rop);
        this.type = type;
    }

    public MipsCmp(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm, MipsCmpType type) {
        super(parent, dest, lop, rimm);
        this.type = type;
    }

    @Override
    protected String getOpcode() {
        return type.getCmp();
    }
}
