package llvmir.tree.value.user.constant.data;

import llvmir.tree.type.Types;

import java.util.Objects;

public class ConstantInt extends ConstantData {
    private final int val;

    public ConstantInt(int val) {
        super(Types.INT, String.valueOf(val));
        this.val = val;
    }

    public int getValue() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ConstantInt && ((ConstantInt) o).val == val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }

    @Override
    public String toString() {
        return valType + " " + val;
    }

    public static final ConstantInt ZERO = new ConstantInt(0);

}
