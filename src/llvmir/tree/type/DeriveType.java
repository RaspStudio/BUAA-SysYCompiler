package llvmir.tree.type;

public abstract class DeriveType extends Type {

    public final Type getDerivedType() {
        return getDerivedType(1);
    }

    public abstract Type getDerivedType(int depth);
}

