package frontend.label.meta;

import frontend.token.meta.KeyWordToken;
import frontend.tree.var.InitValNode;
import util.Pair;

import java.util.List;
import java.util.Objects;

public class VarLabel extends Label {
    private final ValueType type;
    private final InitValNode val;
    private final boolean isConst;

    public VarLabel(String name, KeyWordToken base, boolean needId, List<Integer> dimensions) {
        this(name, base, needId, dimensions, InitValNode.undefined(dimensions), false);
    }

    public VarLabel(String name, KeyWordToken base, List<Integer> dimensions) {
        this(name, base, false, dimensions, InitValNode.undefined(dimensions), false);
    }

    private VarLabel(String name, ValueType type, boolean isConst) {
        super(name, false, LabelType.Value);
        this.type = type;
        this.val = null;
        this.isConst = isConst;
    }

    public VarLabel(String name, KeyWordToken base, List<Integer> dimensions, InitValNode val, boolean isConst) {
        this(name, base, false, dimensions, val, isConst);
    }

    private VarLabel(String name, KeyWordToken base, boolean needId,
                     List<Integer> dimensions, InitValNode val, boolean isConst) {
        super(name, needId, LabelType.Value);
        this.type = new ValueType(base, dimensions);
        this.val = val;
        this.isConst = isConst;
    }

    @Override
    public boolean sameTypeWith(Label label) {
        return label instanceof VarLabel && ((VarLabel) label).type.equals(this.type);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Label && ((Label) o).sameTypeWith(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public VarLabel subLabel() {
        return subLabel(1);
    }

    public VarLabel subLabel(int dive) {
        return new VarLabel(name + "#" + dive, type.subType(dive), isConst);
    }

    public Pair<KeyWordToken, List<Integer>> getType() {
        return type.getType();
    }

    @Override
    public boolean isBase(KeyWordToken type) {
        return this.type.isBase(type);
    }

    public boolean isConst() {
        return isConst;
    }

    public int getValue(List<Integer> dimensions) {
        assert val != null;
        return val.getValue(dimensions);
    }
}
