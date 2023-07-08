package llvmir.tree.value.user.constant.global;

import llvmir.tree.type.Type;
import llvmir.tree.value.Value;

import java.util.List;

public abstract class GlobalObject extends GlobalValue {
    protected GlobalObject(Type valType, String name, List<Value> operands) {
        super(valType, name, operands);
    }
}
