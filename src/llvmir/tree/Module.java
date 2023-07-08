package llvmir.tree;

import llvmir.tree.type.Types;
import llvmir.tree.value.Argument;
import llvmir.tree.value.user.constant.data.ConstantString;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.constant.global.GlobalVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class Module implements SymbolTabler {
    private final List<GlobalVariable> variables;
    private final List<Function> builtInFuncs;
    private final List<Function> functions;
    private final SymbolTable table;

    public Module() {
        this.variables = new ArrayList<>();
        this.table = new SymbolTable();
        this.builtInFuncs = getBuiltInFunctions(this, this.table);
        this.functions = new ArrayList<>();
    }

    public Module register(List<GlobalVariable> globalVars, List<Function> functions) {
        if (this.functions.size() == 0) {
            this.variables.addAll(globalVars);
            this.functions.addAll(functions);
            return this;
        } else {
            throw new RuntimeException("Can't Register Twice");
        }
    }

    public List<GlobalVariable> getVariables() {
        return new ArrayList<>(variables);
    }

    public List<Function> getFunctions() {
        return new ArrayList<>(functions);
    }

    @Override
    public SymbolTable getTable() {
        return table;
    }

    public GlobalVariable addStringConst(ConstantString string) {
        GlobalVariable strVar = new GlobalVariable(string);
        variables.add(strVar);
        return strVar;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        builtInFuncs.forEach(o -> joiner.add(o.toString()));
        variables.forEach(o -> joiner.add(o.toString()));
        functions.forEach(o -> joiner.add(o.toString()));
        return joiner.toString();
    }

    public static List<Function> getBuiltInFunctions(Module module, SymbolTable table) {
        List<Function> builtIns = new ArrayList<>();
        Function getInt = new Function(Function.INPUT_FUNC, module, Types.INT, table, Collections.emptyList());
        Function putint = new Function(Function.OUTPUT_INT_FUNC, module, Types.VOID, table,
                Collections.singletonList(new Argument(Types.INT, "src", 0)));
        Function putstr = new Function(Function.OUTPUT_STR_FUNC, module, Types.VOID, table,
                Collections.singletonList(new Argument(Types.pointer(Types.CHAR), "src", 0)));
        builtIns.add(getInt);
        builtIns.add(putint);
        builtIns.add(putstr);
        table.add(Function.INPUT_FUNC, getInt);
        table.add(Function.OUTPUT_INT_FUNC, putint);
        table.add(Function.OUTPUT_STR_FUNC, putstr);
        return builtIns;
    }

}
