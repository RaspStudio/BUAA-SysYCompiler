package llvmir.tree.value.user.instruction;

import llvmir.tree.Derivative;
import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.User;
import llvmir.tree.value.user.instruction.binary.AddInst;
import llvmir.tree.value.user.instruction.binary.MulInst;
import llvmir.tree.value.user.instruction.binary.SDivInst;
import llvmir.tree.value.user.instruction.binary.SRemInst;
import llvmir.tree.value.user.instruction.binary.SubInst;

import java.util.List;

public abstract class Instruction extends User implements Derivative<BasicBlock> {
    protected BasicBlock parent;

    protected Instruction(Type valType, String name, BasicBlock parent, List<Value> operands) {
        super(valType, name, operands);
        this.parent = parent;
    }

    public int index() {
        return parent.indexOf(this);
    }

    /**
     * 返回该类型指令是否可以合并，减少重复计算
     * @param o 指令类型
     * @return 是否可以合并
     */
    public static boolean mergeable(Class<? extends Instruction> o) {
        return o == AddInst.class || o == SubInst.class || o == MulInst.class ||
                o == SDivInst.class || o == SRemInst.class || o == GetElementPtrInst.class;
    }

    public static boolean isArith(Class<? extends Value> o) {
        return o == AddInst.class || o == SubInst.class || o == MulInst.class ||
                o == SDivInst.class || o == SRemInst.class;
    }

    @Override
    public final BasicBlock getParent() {
        return parent;
    }

    public void setParent(BasicBlock block) {
        parent = block;
    }

    public abstract boolean hasSideEffect();

}
