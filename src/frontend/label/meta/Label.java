package frontend.label.meta;

import frontend.token.meta.KeyWordToken;
import frontend.tree.LabelNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Label {
    protected final String name;
    private final Set<LabelType> types;
    private static int id = 0;

    protected Label(String name, boolean needId, LabelType...types) {
        this.name = needId ? name + "@" + (++id) : name;
        this.types = new HashSet<>(Arrays.asList(types));
    }

    protected Label(String name, LabelType... types) {
        this(name, false, types);
    }

    public final String name() {
        return name;
    }

    @Override
    public abstract boolean equals(Object o);

    public final boolean isType(LabelType type) {
        return types.contains(type);
    }

    public abstract boolean isBase(KeyWordToken type);

    protected abstract boolean sameTypeWith(Label label);

    public static <T extends Label> T deriveLabel(Iterable<? extends LabelNode<T>> derivatives) {
        Set<T> labels = new HashSet<>();
        derivatives.forEach(o -> labels.add(o.label()));
        if (labels.size() != 1) {
            throw new IllegalArgumentException("Different Type Operands!");
        }
        return labels.iterator().next();
    }
}
