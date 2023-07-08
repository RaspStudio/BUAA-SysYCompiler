package llvmir.tree.type;

public class FunctionType extends Type {
    protected FunctionType() {}

    @Override
    public int size() {
        return DEFAULT;
    }

    @Override
    public String toString() {
        return "?func";
    }
}
