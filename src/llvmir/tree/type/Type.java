package llvmir.tree.type;

public abstract class Type {
    public static final int DEFAULT = 32;

    public abstract int size();

    public final int byteSize() {
        return (size() + 7) / 8;
    }

    @Override
    public abstract String toString();
}
