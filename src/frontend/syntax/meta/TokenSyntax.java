package frontend.syntax.meta;

import frontend.syntax.factories.SyntaxType;
import frontend.token.meta.MetaToken;

public class TokenSyntax extends MetaSyntax {
    private final MetaToken token;

    public TokenSyntax(MetaToken token) {
        super(SyntaxType.Wrapper);
        this.token = token;
    }

    @Override
    public String getTokenCode() {
        return token.getTokenCode();
    }

    public static MetaToken getToken(MetaSyntax syntax) {
        if (syntax instanceof TokenSyntax) {
            return ((TokenSyntax) syntax).token;
        }
        throw new UnsupportedOperationException("Failed To Get Token From Syntax!");
    }
}
