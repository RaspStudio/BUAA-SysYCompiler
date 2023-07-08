package frontend.syntax.factories;

import frontend.exception.FrontEndException;
import frontend.exception.Handler;
import frontend.syntax.TokenIterator;
import frontend.syntax.meta.MetaSyntax;
import frontend.syntax.meta.ParseSyntax;

import java.util.ArrayList;
import java.util.List;

import static frontend.syntax.factories.SFunction.SFuncDef;
import static frontend.syntax.factories.SFunction.SMainFuncDef;
import static frontend.syntax.factories.SVariable.ADecl;

public abstract class SCompUnit {
    public static final SAbstract SCompUnit = new SAbstract() {
        @Override
        public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
            List<MetaSyntax> derivatives = new ArrayList<>();
            while (ADecl.match(tokens)) {
                derivatives.add(ADecl.parse(tokens, handler));
            }

            while (SFuncDef.match(tokens)) {
                derivatives.add(SFuncDef.parse(tokens, handler));
            }

            derivatives.add(SMainFuncDef.parse(tokens, handler));

            if (tokens.cur() != null) {
                throw new RuntimeException("Expected Finish All Tokens");
            }

            return new ParseSyntax(derivatives, SyntaxType.CompUnit);
        }

        @Override
        public String toString() {
            return "[Parser:CompUnit]";
        }
    };
}
