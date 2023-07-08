package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.inst.MipsCmpType;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;
import llvmir.tree.value.user.instruction.binary.ICmpInst;
import llvmir.tree.value.user.instruction.terminator.BrInst;

public class MipsCmp extends MipsRInst {
    protected final MipsCmpType type;
    protected final boolean forBranch;

    public MipsCmp(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop, ICmpInst inst) {
        super(parent, dest, lop, rop);
        this.type = MipsCmpType.of(inst.getCmpType());
        this.forBranch = inst.getUsers().size() == 1 && inst.getUsers().get(0) instanceof BrInst;
    }

    public MipsCmp(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm, ICmpInst inst) {
        super(parent, dest, lop, rimm);
        this.type = MipsCmpType.of(inst.getCmpType());
        this.forBranch = inst.getUsers().size() == 1 && inst.getUsers().get(0) instanceof BrInst;
    }

    public MipsCmpType getType() {
        return type;
    }

    public boolean isForBranch() {
        return forBranch;
    }

    @Override
    protected String getOpcode() {
        return type.getCmp();
    }
}
