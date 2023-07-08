package llvmir.tree.type;

import java.util.ArrayList;
import java.util.List;

public class ArrayType extends DeriveType {
    private final Type elementType;
    private final int elementsNum;

    protected ArrayType(Type elementType, int elementsNum) {
        this.elementType = elementType;
        this.elementsNum = elementsNum;
    }

    @Override
    public Type getDerivedType(int depth) {
        if (depth == 0) {
            return this;
        } else if (depth == 1) {
            return elementType;
        } else if (elementType instanceof DeriveType) {
            return ((DeriveType) elementType).getDerivedType(depth - 1);
        } else {
            throw new RuntimeException("Array type depth is not enough");
        }
    }

    public List<Integer> getDimensions() {
        List<Integer> ret = new ArrayList<>();
        ret.add(elementsNum);
        if (elementType instanceof ArrayType) {
            ret.addAll(((ArrayType) elementType).getDimensions());
        }
        return ret;
    }

    @Override
    public int size() {
        return elementsNum * elementType.size();
    }

    @Override
    public String toString() {
        return String.format("[%d x %s]", elementsNum, elementType);
    }
}
