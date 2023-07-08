package frontend.syntax.meta;

import frontend.syntax.factories.SyntaxType;

public abstract class MetaSyntax {
    protected final SyntaxType type;

    protected MetaSyntax(SyntaxType type) {
        this.type = type;
    }

    public abstract String getTokenCode();

    public final boolean isType(SyntaxType type) {
        return type.equals(this.type);
    }
}
