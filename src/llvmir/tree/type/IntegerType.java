package llvmir.tree.type;

public class IntegerType extends Type {
    private final int bits;

    protected IntegerType() {
        this.bits = DEFAULT;
    }

    protected IntegerType(int bits) {
        this.bits = bits;
    }

    @Override
    public int size() {
        return bits;
    }

    @Override
    public String toString() {
        return "i" + bits;
    }
}
