package llvmir.tree.type;

public class LabelType extends Type {
    protected LabelType() {}

    @Override
    public int size() {
        return DEFAULT;
    }

    @Override
    public String toString() {
        return "label";
    }
}
