package frontend;

import frontend.exception.FrontEndException;
import frontend.syntax.Parser;
import frontend.token.Lexer;
import llvmir.tree.Module;

public class FrontEnd {
    private final Lexer lexer;
    private final Parser parser;
    private Module module = null;

    public FrontEnd(String source) {
        this.lexer = new Lexer(source);
        this.parser = new Parser(lexer.result());
    }

    public String lexicalString() {
        return lexer.getTokenCode();
    }

    public String parsedString() throws FrontEndException {
        return parser.getTokenCode();
    }

    public String exceptionString() throws FrontEndException {
        return parser.getExceptionCode();
    }

    public Module buildIR() {
        if (module == null) {
            parser.getRoot();
            if (parser.hasException()) {
                throw new RuntimeException("Cannot build IR when there are exceptions!");
            }
            module = IRBuilder.buildModule(parser.getRoot());
        }
        return module;
    }

}
