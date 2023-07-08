package llvmir.tree.value.user.instruction.terminator;

import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.Collections;

public class RetInst extends TerminateInstruction {

    public RetInst(BasicBlock parent, Value value) {
        super(Types.VOID, "", parent, Collections.singletonList(value));
    }

    public RetInst(BasicBlock parent) {
        super(Types.VOID, "", parent, Collections.emptyList());
    }

    @Override
    public String toString() {
        return operands.size() == 0 ?
                "\tret void" :
                "\tret " + operands.get(0).getType() + " " + operands.get(0).getName();
    }
}
