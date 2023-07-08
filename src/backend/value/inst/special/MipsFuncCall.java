package backend.value.inst.special;

import backend.translate.MipsMapper;
import backend.value.MipsBlock;
import backend.value.MipsFunction;
import backend.value.inst.MipsInst;
import backend.value.inst.atype.MipsLoadWord;
import backend.value.inst.atype.MipsSaveWord;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;

import java.util.List;
import java.util.StringJoiner;

/**
 * 包括保存现场、函数调用和恢复现场的指令
 */
public class MipsFuncCall extends MipsInst {
    private final MipsFunction callee;
    private final MipsMapper mapper;
    private final MipsJal jal;

    public MipsFuncCall(MipsBlock parent, MipsFunction function, MipsMapper mapper, boolean hasReturn) {
        super(parent);
        this.callee = function;
        this.mapper = mapper;
        this.jal = new MipsJal(parent, function, hasReturn);
    }

    @Override
    public List<MipsReg> getDefs() {
        return jal.getDefs();
    }

    @Override
    public List<MipsReg> getUses() {
        return jal.getDefs();
    }

    @Override
    public void replaceUse(MipsReg old, MipsReg newValue) {
        jal.replaceUse(old, newValue);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n\t");
        List<Integer> saveRegs = mapper.getSaveRegs(callee);
        for (int i = 0; i < saveRegs.size(); i++) {
            joiner.add(new MipsSaveWord(parent, MipsReg.of(MipsRegs.of(saveRegs.get(i))),
                    parent.getParent().stack().getCurFuncSave(i)).toString());
        }
        joiner.add(jal.toString());
        for (int i = 0; i < saveRegs.size(); i++) {
            joiner.add(new MipsLoadWord(parent, MipsReg.of(MipsRegs.of(saveRegs.get(i))),
                    parent.getParent().stack().getCurFuncSave(i)).toString());
        }
        return joiner.toString();
    }
}
