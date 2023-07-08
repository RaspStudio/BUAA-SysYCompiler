package frontend.exception;

import frontend.token.meta.MetaToken;
import frontend.token.meta.TokenType;

public class TakeTokenException extends FrontEndException {
    public TakeTokenException(MetaToken expected, MetaToken actual) {
        super(String.format("Take Token Exception: Expect \"%s\", But We Got \"%s\"", expected, actual));
    }

    public TakeTokenException(TokenType expected, MetaToken actual) {
        super(String.format("Take Token Exception: Expect \"%s\", But We Got \"%s\"", expected.name(), actual));
    }
}
