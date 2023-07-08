package llvmir.tree.value.user.constant.global;

import llvmir.tree.Derivative;
import llvmir.tree.Module;
import llvmir.tree.type.Type;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.Constant;

import java.util.List;

public abstract class GlobalValue extends Constant implements Derivative<Module> {
    protected GlobalValue(Type valType, String name, List<Value> operands) {
        super(valType, "@" + name, operands);
    }
}
