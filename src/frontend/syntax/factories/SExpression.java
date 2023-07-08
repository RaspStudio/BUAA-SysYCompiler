package frontend.syntax.factories;

import frontend.exception.ExceptionType;
import frontend.exception.FrontEndException;
import frontend.exception.Handler;
import frontend.exception.TakeTokenException;
import frontend.syntax.TokenIterator;
import frontend.syntax.meta.MetaSyntax;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.IdentifierToken;
import frontend.token.meta.MetaToken;
import frontend.token.meta.SymbolToken;
import frontend.token.meta.TokenType;

import java.util.ArrayList;
import java.util.List;

import static frontend.syntax.factories.SFunction.AFuncCall;

public abstract class SExpression {

    // 逻辑表达式
    public static final SAbstract SCond;
    public static final SAbstract SLOrExp;
    public static final SAbstract SLAndExp;
    public static final SAbstract SEqExp;
    public static final SAbstract SRelExp;

    // 顶层表达式
    public static final SAbstract SExp;
    public static final SAbstract SLVal;
    public static final SAbstract SConstExp;

    // 算数表达式
    public static final SAbstract SAddExp;
    public static final SAbstract SMulExp;
    public static final SAbstract SUnaryExp;
    public static final SAbstract SPrimaryExp;

    // 其它
    public static final SAbstract SNumber;
    public static final SAbstract SUnaryOp;

    private static MetaSyntax parseLDive(SAbstract subSyntax, SyntaxType curType, MetaToken connectToken,
                                         TokenIterator tokens, Handler handler) throws FrontEndException {
        List<MetaSyntax> derivatives = new ArrayList<>();
        derivatives.add(subSyntax.parse(tokens, handler));
        while (tokens.cur().equals(connectToken)) {
            MetaSyntax tempDive = new ParseSyntax(derivatives, curType);
            derivatives.clear();
            derivatives.add(tempDive);
            derivatives.add(tokens.take(connectToken));
            derivatives.add(subSyntax.parse(tokens, handler));
        }
        return new ParseSyntax(derivatives, curType);
    }

    private static MetaSyntax parseLDive(SAbstract subSyntax, SyntaxType curType, TokenType connectType,
                                         TokenIterator tokens, Handler handler) throws FrontEndException {
        List<MetaSyntax> derivatives = new ArrayList<>();
        derivatives.add(subSyntax.parse(tokens, handler));
        while (tokens.cur().isTypeOf(connectType)) {
            MetaSyntax tempDive = new ParseSyntax(derivatives, curType);
            derivatives.clear();
            derivatives.add(tempDive);
            derivatives.add(tokens.takeWithType(connectType));
            derivatives.add(subSyntax.parse(tokens, handler));
        }
        return new ParseSyntax(derivatives, curType);
    }

    private static MetaSyntax parseWrap(SAbstract subSyntax, SyntaxType curType,
                                        TokenIterator tokens, Handler handler) throws FrontEndException {
        List<MetaSyntax> derivatives = new ArrayList<>();
        derivatives.add(subSyntax.parse(tokens, handler));
        return new ParseSyntax(derivatives, curType);
    }

    public static MetaSyntax parseWrap(TokenType subTokenType, SyntaxType curType,
                                        TokenIterator tokens) throws FrontEndException {
        List<MetaSyntax> token = new ArrayList<>();
        token.add(tokens.takeWithType(subTokenType));
        return new ParseSyntax(token, curType);
    }

    static {
        // 逻辑表达式
        SCond = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseWrap(SLOrExp, SyntaxType.Cond, tokens, handler);
            }
        };

        SLOrExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseLDive(SLAndExp, SyntaxType.LOrExp, SymbolToken.ORSYM, tokens, handler);
            }
        };

        SLAndExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseLDive(SEqExp, SyntaxType.LAndExp, SymbolToken.ANDSYM, tokens, handler);
            }
        };

        SEqExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseLDive(SRelExp, SyntaxType.EqExp, TokenType.EqOp, tokens, handler);
            }
        };

        SRelExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseLDive(SAddExp, SyntaxType.RelExp, TokenType.RelOp, tokens, handler);
            }
        };

        // 顶层表达式
        SExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseWrap(SAddExp, SyntaxType.Exp, tokens, handler);
            }
        };

        SLVal = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.takeWithType(TokenType.Ident));
                while (tokens.cur().equals(SymbolToken.LBRACKSYM)) {
                    derivatives.add(tokens.take(SymbolToken.LBRACKSYM));
                    derivatives.add(SExp.parse(tokens, handler));
                    try {
                        derivatives.add(tokens.take(SymbolToken.RBRACKSYM));
                    } catch (TakeTokenException e) {
                        derivatives.add(new TokenSyntax(SymbolToken.RBRACKSYM));
                        handler.save(e, ExceptionType.MissingRBrack, tokens.lastPosition());
                    }
                }
                return new ParseSyntax(derivatives, SyntaxType.LVal);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur() instanceof IdentifierToken && !tokens.peek().equals(SymbolToken.LPARENTSYM);
            }
        };

        SConstExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseWrap(SAddExp, SyntaxType.ConstExp, tokens, handler);
            }
        };

        // 算数表达式
        SAddExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseLDive(SMulExp, SyntaxType.AddExp, TokenType.AddOp, tokens, handler);
            }
        };

        SMulExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseLDive(SUnaryExp, SyntaxType.MulExp, TokenType.MulOp, tokens, handler);
            }
        };

        SUnaryExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                if (tokens.cur().isTypeOf(TokenType.UnaryOp)) {
                    derivatives.add(SUnaryOp.parse(tokens, handler));
                    derivatives.add(SUnaryExp.parse(tokens, handler));
                    return new ParseSyntax(derivatives, SyntaxType.UnaryExp);
                } else if (AFuncCall.match(tokens)) {
                    return AFuncCall.parse(tokens, handler);
                } else {
                    derivatives.add(SPrimaryExp.parse(tokens, handler));
                    return new ParseSyntax(derivatives, SyntaxType.UnaryExp);
                }
            }
        };

        SPrimaryExp = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                if (tokens.cur().equals(SymbolToken.LPARENTSYM)) {
                    derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
                    derivatives.add(SExp.parse(tokens, handler));
                    try {
                        derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
                    } catch (TakeTokenException e) {
                        derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
                        handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
                    }
                } else if (SNumber.match(tokens)) {
                    derivatives.add(SNumber.parse(tokens, handler));
                } else {
                    derivatives.add(SLVal.parse(tokens, handler));
                }
                return new ParseSyntax(derivatives, SyntaxType.PrimaryExp);
            }
        };

        // 其它
        SUnaryOp = (tokens, handler) -> parseWrap(TokenType.UnaryOp, SyntaxType.UnaryOp, tokens);

        SNumber = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseWrap(TokenType.Integer, SyntaxType.Number, tokens);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().isTypeOf(TokenType.Integer);
            }
        };

    }

}
