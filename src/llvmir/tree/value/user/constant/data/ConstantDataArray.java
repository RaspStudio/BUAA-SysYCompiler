package llvmir.tree.value.user.constant.data;

import llvmir.tree.type.Type;
import llvmir.tree.value.user.constant.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ConstantDataArray extends ConstantDataSequential {
    private final List<Constant> data;

    public ConstantDataArray(Type arrayType, String name, List<Constant> array) {
        super(arrayType, name);
        this.data = array;
    }

    public List<Integer> getValues() {
        if (data.get(0) instanceof ConstantInt) {
            List<Integer> ret = new ArrayList<>();
            data.forEach(o -> ret.add(((ConstantInt)o).getValue()));
            return ret;
        } else if (data.get(0) instanceof ConstantDataArray) {
            List<Integer> ret = new ArrayList<>();
            data.forEach(o -> ret.addAll(((ConstantDataArray)o).getValues()));
            return ret;
        } else {
            throw new RuntimeException("Unsupported data type: " + data.get(0).getType());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(valType).append(" ").append("[");
        StringJoiner joiner = new StringJoiner(", ");
        data.forEach(o -> joiner.add(o.toString()));
        sb.append(joiner).append("]");
        return sb.toString();
    }

}
