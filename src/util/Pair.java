package util;

import java.util.Objects;

public class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pair && ((Pair<?, ?>) o).left.equals(left) && ((Pair<?, ?>) o).right.equals(right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }
}
