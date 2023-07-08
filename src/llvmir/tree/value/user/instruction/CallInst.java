package llvmir.tree.value.user.instruction;

import llvmir.tree.type.IntegerType;
import llvmir.tree.type.Type;
import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.global.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CallInst extends NormalInstruction {
    private final boolean isBuiltIn;

    protected CallInst(Type valType, String name, BasicBlock parent, List<Value> operands) {
        super(valType, name, parent, operands);
        isBuiltIn = ((Function)(operands.get(0))).isBuiltIn();
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    public static CallInst createCall(Function func, BasicBlock parent, List<Value> rparams) {
        List<Value> operands = new ArrayList<>();
        operands.add(func);
        operands.addAll(rparams);
        if (func.getRetType() instanceof IntegerType) {
            return new CallInst(Types.INT,
                    func.getName().replace('@', '%') + ".call" + Value.allocId(func.getName()),
                    parent, operands);
        } else {
            return new CallInst(Types.VOID, "", parent, operands);
        }
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name.length() == 0 ? "call " : name + " = call ");
        builder.append(valType).append(" ").append(operands.get(0).getName()).append("(");

        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 1; i < operands.size(); i++) {
            joiner.add(operands.get(i).getType() + " " + operands.get(i).getName());
        }

        return "\t" + builder.append(joiner).append(")");
    }
}
