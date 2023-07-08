package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.binary.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class MipsRInst extends MipsInst {
    protected MipsReg dest;
    protected MipsReg lop;
    protected MipsReg rop;
    protected MipsImm rimm;
    protected boolean isImmType;

    protected MipsRInst(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop) {
        super(parent);
        this.dest = dest;
        this.lop = lop;
        this.rop = rop;
        this.rimm = null;
        this.isImmType = false;
        dest.setDef(this);
        lop.addUser(this);
        rop.addUser(this);
    }

    protected MipsRInst(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm) {
        super(parent);
        this.dest = dest;
        this.lop = lop;
        this.rop = null;
        this.rimm = rimm;
        this.isImmType = true;
        dest.setDef(this);
        lop.addUser(this);
    }

    protected abstract String getOpcode();

    @Override
    public final List<MipsReg> getDefs() {
        return Collections.singletonList(dest);
    }

    @Override
    public final List<MipsReg> getUses() {
        return isImmType ? Collections.singletonList(lop) : Arrays.asList(lop, rop);
    }

    public MipsImm getRimm() {
        return rimm;
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        if (dest == old) {
            throw new RuntimeException("Cannot replace dest");
        } else if (lop == old) {
            lop = newValue;
        } else if (rop == old) {
            rop = newValue;
        } else {
            throw new RuntimeException("No such use");
        }
    }

    @Override
    public String toString() {
        return (getOpcode() + "    ").substring(0, 4) + "\t\t" + dest + ", " + lop + ", " + (isImmType ? rimm : rop);
    }

    public static MipsRInst create(MipsBlock parent, MipsReg dest, MipsReg lop, Instruction inst, MipsImm imm) {
        return create(parent, dest, lop, inst, null, imm);
    }

    public static MipsRInst create(MipsBlock parent, MipsReg dest, MipsReg lop, Instruction inst, MipsReg reg) {
        return create(parent, dest, lop, inst, reg, null);
    }

    private static MipsRInst create(MipsBlock parent, MipsReg dest, MipsReg lop, Instruction inst,
                                   MipsReg reg, MipsImm imm) {
        if (inst instanceof AddInst) {
            return reg == null ?
                    new MipsAdd(parent, dest, lop, imm) :
                    new MipsAdd(parent, dest, lop, reg);
        } else if (inst instanceof SubInst) {
            return reg == null ?
                    new MipsSub(parent, dest, lop, imm) :
                    new MipsSub(parent, dest, lop, reg);
        } else if (inst instanceof MulInst) {
            return reg == null ?
                    new MipsMul(parent, dest, lop, imm) :
                    new MipsMul(parent, dest, lop, reg);
        } else if (inst instanceof SDivInst) {
            return reg == null ?
                    new MipsDiv(parent, dest, lop, imm) :
                    new MipsDiv(parent, dest, lop, reg);
        } else if (inst instanceof SRemInst) {
            return reg == null ?
                    new MipsRem(parent, dest, lop, imm) :
                    new MipsRem(parent, dest, lop, reg);
        } else if (inst instanceof ICmpInst) {
            return reg == null ?
                    new MipsCmp(parent, dest, lop, imm, (ICmpInst) inst) :
                    new MipsCmp(parent, dest, lop, reg, (ICmpInst) inst);
        } else {
            throw new RuntimeException("Unknown binary operator");
        }
    }

}
