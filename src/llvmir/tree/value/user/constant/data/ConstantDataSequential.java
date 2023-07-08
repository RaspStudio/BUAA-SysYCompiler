package llvmir.tree.value.user.constant.data;

import llvmir.tree.type.Type;

public abstract class ConstantDataSequential extends ConstantData {

    protected ConstantDataSequential(Type valType, String name) {
        super(valType, name);
    }
}
