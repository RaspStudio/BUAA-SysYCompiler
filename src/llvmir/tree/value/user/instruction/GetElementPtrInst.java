package llvmir.tree.value.user.instruction;

import llvmir.tree.type.DeriveType;
import llvmir.tree.type.PointerType;
import llvmir.tree.type.Type;
import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.global.GlobalVariable;
import util.Tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GetElementPtrInst extends NormalInstruction {
    private final Type sequenceType;

    /**
     * 传入的参数中，第一个参数类型是 pointer(array(i8))，本函数构造的指令返回的类型是 char*
     */
    public GetElementPtrInst(GlobalVariable strConst, BasicBlock parent) {
        super(
                Types.pointer(((DeriveType)(strConst.getType())).getDerivedType(2)),
                "%ptr." + Value.allocId("GETELEMENTPTR"),
                parent, Arrays.asList(strConst, new ConstantInt(0), new ConstantInt(0))
        );
        this.sequenceType = ((DeriveType)(strConst.getType())).getDerivedType();
    }

    private GetElementPtrInst(DeriveType arrayType, BasicBlock parent, Value ptrs, List<Value> objIndex) {
        super(
                Types.pointer(arrayType.getDerivedType(objIndex.size())),
                "%ptr." + Value.allocId("GETELEMENTPTR"),
                parent, Tools.merge(Collections.singletonList(ptrs), objIndex)
        );
        this.sequenceType = arrayType.getDerivedType();
    }

    public static GetElementPtrInst build(BasicBlock parent, Value dest, List<Value> objIndex) {
        List<Value> indices = Tools.merge(Collections.singletonList(new ConstantInt(0)), objIndex);
        Type curType = dest.getType();
        Value curDest = dest;
        if (curType instanceof PointerType && ((PointerType) curType).getDerivedType() instanceof PointerType) {
            curDest = new LoadInst(parent, curDest);
            parent.addInst((NormalInstruction) curDest);
            curType = curDest.getType();
            indices.remove(0);
        }
        return new GetElementPtrInst((DeriveType) curType, parent, curDest, indices);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\t" + name + " = getelementptr " + sequenceType);
        for (Value o : operands) {
            sb.append(", ").append(o.getType().toString()).append(" ").append(o.getName());
        }
        return sb.toString();
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }
}
