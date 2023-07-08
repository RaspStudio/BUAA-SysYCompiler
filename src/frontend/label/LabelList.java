package frontend.label;

import frontend.exception.SyntaxException;
import frontend.label.meta.Label;
import frontend.label.meta.VarLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelList {
    // 符号表层次
    private final LabelList parent;
    private final List<LabelList> sons;

    // 符号表内容
    private final Map<String, Label> labels;

    private LabelList() {
        this.parent = null;
        this.sons = new ArrayList<>();
        this.labels = new HashMap<>();
    }

    private LabelList(LabelList parent) {
        this.parent = parent;
        this.sons = new ArrayList<>();
        this.labels = new HashMap<>();
    }

    public int getValue(String s, List<Integer> dimensions) throws SyntaxException {
        Label src = get(s);
        if (src instanceof VarLabel && ((VarLabel) src).isConst()) {
            return ((VarLabel) src).getValue(dimensions);
        }
        throw new IllegalArgumentException("Not An Const Value");
    }

    public Label get(String s) throws SyntaxException {
        if (has(s, false)) {
            return labels.get(s);
        } else if (has(s, true) && parent != null) {
            return parent.get(s);
        }
        throw new SyntaxException("Unknown Label");
    }

    public boolean has(String s, boolean recursive) {
        if (labels.containsKey(s)) {
            return true;
        } else if (recursive && parent != null) {
            return parent.has(s, true);
        } else {
            return false;
        }
    }

    public void add(Label label) throws SyntaxException {
        if (has(label.name(), false)) {
            throw new SyntaxException("Duplicated Label");
        }
        labels.put(label.name(), label);
    }

    public boolean isTop() {
        return parent == null;
    }

    public static LabelList init() {
        return new LabelList();
    }

    public LabelList derive() {
        LabelList newSon = new LabelList(this);
        this.sons.add(newSon);
        return newSon;
    }
}
