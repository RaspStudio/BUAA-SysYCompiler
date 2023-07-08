package llvmir.pass.dag;

import llvmir.tree.value.Value;

import java.util.ArrayList;
import java.util.List;

public class SDNode {
    private final int id;
    private final String name;
    private final List<Value> origins = new ArrayList<>();
    private final List<SDNode> users = new ArrayList<>();
    private final List<SDNode> operands = new ArrayList<>();

    public SDNode(int id, String name, Value origin, List<SDNode> operands) {
        this.name = name;
        this.id = id;
        this.origins.add(origin);
        this.operands.addAll(operands);
        operands.forEach(operand -> operand.users.add(this));
    }

    public List<Value> getOrigins() {
        return origins;
    }

    public List<Integer> getOperandIds() {
        List<Integer> ids = new ArrayList<>();
        operands.forEach(operand -> ids.add(operand.id));
        return ids;
    }

    public int getId() {
        return id;
    }

}
