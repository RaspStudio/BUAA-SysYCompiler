package frontend.syntax.factories;

import frontend.exception.Handler;
import frontend.exception.ExceptionType;
import frontend.exception.FrontEndException;
import frontend.exception.TakeTokenException;
import frontend.syntax.TokenIterator;
import frontend.syntax.meta.MetaSyntax;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.IdentifierToken;
import frontend.token.meta.KeyWordToken;
import frontend.token.meta.MetaToken;
import frontend.token.meta.SymbolToken;

import java.util.ArrayList;
import java.util.List;

import static frontend.syntax.factories.SExpression.SConstExp;
import static frontend.syntax.factories.SExpression.SExp;
import static frontend.token.meta.TokenType.BType;
import static frontend.token.meta.TokenType.Ident;

/**
 * 覆盖了 6 种有标识符非终结符和 1 种无标识符非终结符
 */
public abstract class SVariable {
    public static final SAbstract ADecl;
    public static final SAbstract SConstDecl;
    public static final SAbstract SConstDef;
    public static final SAbstract SConstInitVal;
    public static final SAbstract SVarDecl;
    public static final SAbstract SVarDef;
    public static final SAbstract SInitVal;

    private static MetaSyntax parseDef(boolean isConst, TokenIterator tokens, Handler handler)
            throws FrontEndException {
        List<MetaSyntax> derivatives = new ArrayList<>();
        derivatives.add(tokens.takeWithType(Ident));
        while (tokens.cur().equals(SymbolToken.LBRACKSYM)) {
            derivatives.add(tokens.take(SymbolToken.LBRACKSYM));
            derivatives.add(SConstExp.parse(tokens, handler));
            try {
                derivatives.add(tokens.take(SymbolToken.RBRACKSYM));
            } catch (TakeTokenException e) {
                derivatives.add(new TokenSyntax(SymbolToken.RBRACKSYM));
                handler.save(e, ExceptionType.MissingRBrack, tokens.lastPosition());
            }
        }
        if (tokens.cur().equals(SymbolToken.ASSIGNSYM) || isConst) {
            derivatives.add(tokens.take(SymbolToken.ASSIGNSYM));
            derivatives.add(isConst ?
                    SConstInitVal.parse(tokens, handler) :
                    SInitVal.parse(tokens, handler));
        }

        return new ParseSyntax(derivatives, isConst ? SyntaxType.ConstDef : SyntaxType.VarDef);
    }

    private static MetaSyntax parseInitVal(boolean isConst, TokenIterator tokens, Handler handler)
            throws FrontEndException {
        List<MetaSyntax> derivatives = new ArrayList<>();
        if (tokens.cur().equals(SymbolToken.LBRACESYM)) {
            derivatives.add(tokens.take(SymbolToken.LBRACESYM));
            if (!tokens.cur().equals(SymbolToken.RBRACESYM)) {
                derivatives.add(parseInitVal(isConst, tokens, handler));
                while (tokens.cur().equals(SymbolToken.COMMASYM)) {
                    derivatives.add(tokens.take(SymbolToken.COMMASYM));
                    derivatives.add(parseInitVal(isConst, tokens, handler));
                }
            }
            derivatives.add(tokens.take(SymbolToken.RBRACESYM));
        } else {
            derivatives.add(isConst ? SConstExp.parse(tokens, handler) : SExp.parse(tokens, handler));
        }
        return new ParseSyntax(derivatives, isConst ? SyntaxType.ConstInitVal : SyntaxType.InitVal);
    }

    static {
        // 声明头
        ADecl = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                if (SConstDecl.match(tokens)) {
                    return SConstDecl.parse(tokens, handler);
                } else {
                    return SVarDecl.parse(tokens, handler);
                }
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return SConstDecl.match(tokens) || SVarDecl.match(tokens);
            }
        };

        // 声明单组
        SConstDecl = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.take(KeyWordToken.CONSTTK));
                derivatives.add(tokens.takeWithType(BType));
                derivatives.add(SConstDef.parse(tokens, handler));
                while (tokens.cur().equals(SymbolToken.COMMASYM)) {
                    derivatives.add(tokens.take(SymbolToken.COMMASYM));
                    derivatives.add(SConstDef.parse(tokens, handler));
                }
                try {
                    derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                    handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
                }
                return new ParseSyntax(derivatives, SyntaxType.ConstDecl);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                MetaToken t0 = tokens.peek(0);
                MetaToken t1 = tokens.peek(1);
                MetaToken t2 = tokens.peek(2);
                MetaToken t3 = tokens.peek(3);
                // ConstDecl
                return t0.equals(KeyWordToken.CONSTTK)
                        && t1.isTypeOf(BType)
                        && t2 instanceof IdentifierToken
                        && (t3.equals(SymbolToken.ASSIGNSYM)
                        || t3.equals(SymbolToken.LBRACKSYM));
            }
        };

        SVarDecl = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.takeWithType(BType));
                derivatives.add(SVarDef.parse(tokens, handler));
                while (tokens.cur().equals(SymbolToken.COMMASYM)) {
                    derivatives.add(tokens.take(SymbolToken.COMMASYM));
                    derivatives.add(SVarDef.parse(tokens, handler));
                }
                try {
                    derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                    handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
                }
                return new ParseSyntax(derivatives, SyntaxType.VarDecl);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                MetaToken t0 = tokens.peek(0);
                MetaToken t1 = tokens.peek(1);
                MetaToken t2 = tokens.peek(2);

                // VarDecl
                return t0.isTypeOf(BType)
                        && t1 instanceof IdentifierToken
                        && !t2.equals(SymbolToken.LPARENTSYM);
            }
        };

        // 声明单体
        SConstDef = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseDef(true, tokens, handler);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur() instanceof IdentifierToken;
            }
        };

        SVarDef = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseDef(false, tokens, handler);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur() instanceof IdentifierToken;
            }
        };

        // 声明初值
        SConstInitVal = (tokens, handler) -> parseInitVal(true, tokens, handler);

        SInitVal = (tokens, handler) -> parseInitVal(false, tokens, handler);
    }
}
