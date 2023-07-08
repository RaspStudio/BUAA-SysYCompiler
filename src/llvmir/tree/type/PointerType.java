package llvmir.tree.type;

public class PointerType extends DeriveType {
    private final Type pointee;

    protected PointerType(Type pointee) {
        this.pointee = pointee;
    }

    @Override
    public Type getDerivedType(int depth) {
        if (depth == 0) {
            return this;
        } else if (depth == 1) {
            return pointee;
        } else if (pointee instanceof DeriveType) {
            return ((DeriveType) pointee).getDerivedType(depth - 1);
        } else {
            throw new RuntimeException("Cannot Dive in: " + depth);
        }
    }

    @Override
    public int size() {
        return DEFAULT;
    }

    @Override
    public String toString() {
        return pointee + "*";
    }
}
