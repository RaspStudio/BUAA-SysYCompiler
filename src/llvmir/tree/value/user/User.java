package llvmir.tree.value.user;

import llvmir.tree.type.Type;
import llvmir.tree.value.Value;

import java.util.ArrayList;
import java.util.List;

public abstract class User extends Value {
    protected final List<Value> operands;

    protected User(Type valType, String name, List<Value> operands) {
        super(valType, name);
        this.operands = new ArrayList<>(operands);
        operands.forEach(op -> op.addUser(this));
    }

    public Value getOperand(int index) {
        return index < operands.size() ? operands.get(index) : null;
    }

    public List<Value> getOperands() {
        return new ArrayList<>(operands);
    }

    public void replaceUse(Value value, Value newValue) {
        for (int i = 0; i < operands.size(); i++) {
            Value op = operands.get(i);
            if (op.equals(value)) {
                operands.set(i, newValue);
                newValue.addUser(this);
            }
        }
    }
}
