package llvmir.tree.type;

public abstract class Types {
    public static final IntegerType INT = new IntegerType();
    public static final IntegerType CHAR = new IntegerType(8);
    public static final IntegerType BOOL = new IntegerType(1);
    public static final VoidType VOID = new VoidType();

    public static PointerType pointer(Type pointee) {
        return new PointerType(pointee);
    }

    public static LabelType label() {
        return new LabelType();
    }

    public static ArrayType array(Type element, int num) {
        return new ArrayType(element, num);
    }

    public static FunctionType function() {
        return new FunctionType();
    }
}
