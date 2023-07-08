package llvmir.tree.type;

public class VoidType extends Type  {
    protected VoidType() {}

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString() {
        return "void";
    }
}
