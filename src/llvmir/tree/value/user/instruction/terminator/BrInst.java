package llvmir.tree.value.user.instruction.terminator;

import llvmir.tree.type.Types;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.StringJoiner;

public class BrInst extends TerminateInstruction {

    public BrInst(BasicBlock parent, BasicBlock to) {
        super(Types.VOID, "", parent, Collections.singletonList(to));
        parent.addTo(to);
        to.addFrom(parent);
    }

    public BrInst(BasicBlock parent, Value cond, BasicBlock trueTo, BasicBlock falseTo) {
        super(Types.VOID, "", parent, Arrays.asList(cond, trueTo, falseTo));
        parent.addTo(trueTo, falseTo);
        trueTo.addFrom(parent);
        falseTo.addFrom(parent);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        operands.forEach(o -> joiner.add(o.getType() + " " + o.getName()));
        return "\tbr " + joiner;
    }
}
