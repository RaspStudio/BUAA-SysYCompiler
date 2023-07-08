package llvmir.tree.value.user.instruction;

import llvmir.tree.type.Type;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantInt;
import util.Pair;

import java.util.*;

public class PhiInst extends Instruction {
    private final Map<BasicBlock, Value> incoming = new HashMap<>();

    public PhiInst(Type valType, String name, BasicBlock parent) {
        super(valType, name, parent, Collections.emptyList());
        parent.getFroms().forEach(bb -> addIncoming(ConstantInt.UNDEFINED, bb));
        if (parent.getFroms().isEmpty()) {
            throw new RuntimeException("PhiInst must have at least one incoming");
        }
    }

    public void addIncoming(Value val, BasicBlock bb) {
        if (incoming.get(bb) != ConstantInt.UNDEFINED && incoming.containsKey(bb)) {
            throw new RuntimeException("Cannot add incoming twice");
        } else if (!parent.getFroms().contains(bb)) {
            throw new RuntimeException("Cannot add incoming from a non-ancestor block");
        } else {
            incoming.put(bb, val);
            bb.addUser(this);
            val.addUser(this);
            // 更新参数集
            operands.clear();
            for (BasicBlock b : incoming.keySet()) {
                operands.add(incoming.get(b));
                operands.add(b);
            }
        }
    }

    public List<Pair<Value, BasicBlock>> getIncoming() {
        List<Pair<Value, BasicBlock>> ret = new ArrayList<>();
        for (int i = 0; i < operands.size(); i += 2) {
            ret.add(new Pair<>(operands.get(i), (BasicBlock) operands.get(i + 1)));
        }
        return ret;
    }

    public BasicBlock getPhiParent(Instruction inst) {
        int index = operands.indexOf(inst);
        if (index == -1) {
            throw new RuntimeException("PhiInst does not have this incoming");
        } else {
            return (BasicBlock) operands.get(index + 1);
        }
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < operands.size(); i += 2) {
            joiner.add("[" + operands.get(i).getName() + ", " + operands.get(i + 1).getName() + "]");
        }
        return "\t" + name + " = phi " + valType + " " + joiner;
    }

    @Override
    public boolean hasSideEffect() {
        return false;
    }

}
