package llvmir.tree.value.user.constant;

import llvmir.tree.type.Type;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.User;

import java.util.List;

public abstract class Constant extends User {

    protected Constant(Type valType, String name, List<Value> operands) {
        super(valType, name, operands);
    }
}
