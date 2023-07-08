package backend.value.inst.special;

import backend.value.MipsBlock;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsReg;

import java.util.List;

public abstract class MipsCallInst extends MipsInst {
    protected final List<MipsReg> uses;
    protected final List<MipsReg> defs;

    public MipsCallInst(MipsBlock parent, List<MipsReg> uses, List<MipsReg> defs) {
        super(parent);
        this.uses = uses;
        this.defs = defs;
        uses.forEach(o -> o.addUser(this));
        defs.forEach(o -> o.setDef(this));
    }

    @Override
    public final List<MipsReg> getDefs() {
        return defs;
    }

    @Override
    public final List<MipsReg> getUses() {
        return uses;
    }
}
