package llvmir.tree.value.user.constant.data;

import llvmir.tree.type.Type;
import llvmir.tree.value.user.constant.Constant;

import java.util.Collections;

public abstract class ConstantData extends Constant {
    protected ConstantData(Type valType, String name) {
        super(valType, name, Collections.emptyList());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ConstantData && ((ConstantData) o).name.equals(name);
    }
}
