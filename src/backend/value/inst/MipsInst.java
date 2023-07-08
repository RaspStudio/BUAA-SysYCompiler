package backend.value.inst;

import backend.regalloc.VInst;
import backend.value.MipsBlock;
import backend.value.meta.MipsReg;

/**
 * 所有指令需要在使用时更新虚拟寄存器的 User
 */
public abstract class MipsInst implements VInst<MipsReg> {

    protected final MipsBlock parent;

    protected MipsInst(MipsBlock parent) {
        this.parent = parent;
    }

    @Override
    public abstract String toString();

    public MipsBlock getParent() {
        return parent;
    }
}
