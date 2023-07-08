package backend.translate;

import backend.value.meta.MipsAddr;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;

import java.util.function.Supplier;

public class MipsStack {
    private int subFuncArgCnt = 4;
    private int curFuncSaveCnt = 0;
    private int curFuncDataCnt = 0;

    /*========== 调用函数注册参数个数、更新函数存储的寄存器数 ==========*/
    public void updateFuncArgCnt(int cnt) {
        subFuncArgCnt = Math.max(subFuncArgCnt, cnt);
    }

    public void updateFuncSaveCnt(int cnt) {
        curFuncSaveCnt = cnt;
    }

    /*========== 栈大小引用地址区（从栈底到栈顶布局） ==========*/
    public MipsAddr getSubFuncArg(int argNo) {
        return MipsAddr.of(null, MipsImm.of(subFuncArg(argNo)), MipsReg.of(MipsRegs.SP));
    }

    public MipsAddr getCurFuncSave(int saveNo) {
        return MipsAddr.of(null, MipsImm.of(curFuncSave(saveNo)), MipsReg.of(MipsRegs.SP));
    }

    public MipsAddr getCurFuncRa() {
        return MipsAddr.of(null, MipsImm.of(curFuncRa()), MipsReg.of(MipsRegs.SP));
    }

    public MipsAddr getCurFuncRet() {
        return MipsAddr.of(null, MipsImm.of(curFuncRet()), MipsReg.of(MipsRegs.SP));
    }

    public MipsAddr allocData(int bytes) {
        int start = curFuncDataCnt;
        curFuncDataCnt += (bytes + 3) / 4;
        return MipsAddr.of(null, MipsImm.of(curFuncData(start)), MipsReg.of(MipsRegs.SP));
    }

    public MipsAddr getCurFuncArg(int argNo) {
        return MipsAddr.of(null, MipsImm.of(curFuncArg(argNo)), MipsReg.of(MipsRegs.SP));
    }

    /*========== 原始栈大小引用区（从栈底到栈顶布局） ==========*/
    private Supplier<Integer> subFuncArg(int argNo) {
        return () -> (argNo) * 4;
    }

    private Supplier<Integer> curFuncSave(int saveNo) {
        return () -> (subFuncArgCnt + saveNo) * 4;
    }

    private Supplier<Integer> curFuncRa() {
        return () -> (subFuncArgCnt + curFuncSaveCnt) * 4;
    }

    private Supplier<Integer> curFuncRet() {
        return () -> (subFuncArgCnt + curFuncSaveCnt + 1) * 4;
    }

    private Supplier<Integer> curFuncNonData() {
        // 下方数据对齐到双字大小
        return () -> ((subFuncArgCnt + curFuncSaveCnt + 2 + 1) / 2 * 2) * 4;
    }

    private Supplier<Integer> curFuncData(int dataNo) {
        return () -> curFuncNonData().get() + dataNo * 4;
    }

    public Supplier<Integer> curStackSize() {
        // 整个栈对齐到双字大小
        return () -> curFuncNonData().get() + (((curFuncDataCnt + 1) / 2 * 2) * 4);
    }

    private Supplier<Integer> curFuncArg(int argNo) {
        return () -> curStackSize().get() + argNo * 4;
    }

    @Override
    public String toString() {
        return "Stack Info: <Arg - " + (4 * subFuncArgCnt) +
                " - Save - " + curFuncRa().get() +
                " - Ra, Ret - " + (curFuncRet().get() + 4) + " - blank - " + curFuncNonData().get() +
                " - alloced - " + curStackSize().get() + ">";
    }
}
