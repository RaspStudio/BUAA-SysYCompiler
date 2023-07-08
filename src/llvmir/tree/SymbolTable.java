package llvmir.tree;

import llvmir.tree.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Value> nameToValue;
    private final SymbolTable parentList;
    private final List<SymbolTable> subList;

    public SymbolTable() {
        this(null);
    }

    public SymbolTable(SymbolTable parentList) {
        this.nameToValue = new HashMap<>();
        this.parentList = parentList;
        this.subList = new ArrayList<>();
    }

    public SymbolTable derive() {
        SymbolTable ret = new SymbolTable(this);
        subList.add(ret);
        return ret;
    }

    public Value get(String name) {
        if (nameToValue.containsKey(name)) {
            return nameToValue.get(name);
        } else if (parentList != null) {
            return parentList.get(name);
        } else {
            throw new RuntimeException("No Such Symbol!");
        }
    }

    public void add(String name, Value value) {
        if (nameToValue.containsKey(name)) {
            throw new RuntimeException("Duplicated Symbol! Check FrontEnd!");
        }
        nameToValue.put(name, value);
    }
}
