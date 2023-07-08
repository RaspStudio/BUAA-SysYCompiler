package frontend.exception;

import frontend.token.meta.MetaToken;

public class SyntaxException extends FrontEndException {
    private final MetaToken token;

    public SyntaxException(String s, MetaToken token) {
        super(s);
        this.token = token;
    }

    public SyntaxException(String s) {
        super(s);
        this.token = null;
    }

    public MetaToken getToken() {
        if (token != null) {
            return token;
        }
        throw new NullPointerException();
    }
}
