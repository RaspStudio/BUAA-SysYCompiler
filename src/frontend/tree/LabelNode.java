package frontend.tree;

import frontend.label.meta.Label;

public interface LabelNode<T extends Label> {
    T label();
}
