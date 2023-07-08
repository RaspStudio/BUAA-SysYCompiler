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
import frontend.token.meta.TokenType;

import java.util.ArrayList;
import java.util.List;

import static frontend.syntax.factories.SExpression.SConstExp;
import static frontend.syntax.factories.SExpression.SExp;
import static frontend.syntax.factories.SStatement.SBlock;

public abstract class SFunction {

    public static final SAbstract SFuncDef;
    public static final SAbstract SMainFuncDef;
    public static final SAbstract SFuncType;
    public static final SAbstract SFuncFParams;
    public static final SAbstract SFuncFParam;
    public static final SAbstract AFuncCall;
    public static final SAbstract SFuncRParams;

    private static MetaSyntax parseFuncDef(boolean isMain, TokenIterator tokens, Handler handler)
            throws FrontEndException {
        List<MetaSyntax> derivatives = new ArrayList<>();
        if (isMain) {
            derivatives.add(tokens.take(KeyWordToken.INTTK));
            derivatives.add(tokens.take(KeyWordToken.MAINTK));
            derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
        } else {
            derivatives.add(SFuncType.parse(tokens, handler));
            derivatives.add(tokens.takeWithType(TokenType.Ident));
            derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
            if (SFuncFParams.match(tokens)) {
                derivatives.add(SFuncFParams.parse(tokens, handler));
            }
        }

        try {
            derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
        } catch (TakeTokenException e) {
            derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
            handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
        }

        derivatives.add(SBlock.parse(tokens, handler));
        return new ParseSyntax(derivatives, isMain ? SyntaxType.MainFuncDef : SyntaxType.FuncDef);
    }

    static {
        SFuncDef = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                return parseFuncDef(false, tokens, handler);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                MetaToken t0 = tokens.peek(0);
                MetaToken t1 = tokens.peek(1);
                MetaToken t2 = tokens.peek(2);

                return t0.isTypeOf(TokenType.FuncType)
                        && t1 instanceof IdentifierToken
                        && t2.equals(SymbolToken.LPARENTSYM);
            }
        };

        SMainFuncDef = (tokens, handler) -> parseFuncDef(true, tokens, handler);

        SFuncType = (tokens, handler) -> SExpression.parseWrap(TokenType.FuncType, SyntaxType.FuncType, tokens);

        SFuncFParams = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(SFuncFParam.parse(tokens, handler));
                while (tokens.cur().equals(SymbolToken.COMMASYM)) {
                    derivatives.add(tokens.take(SymbolToken.COMMASYM));
                    derivatives.add(SFuncFParam.parse(tokens, handler));
                }
                return new ParseSyntax(derivatives, SyntaxType.FuncFParams);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return SFuncFParam.match(tokens);
            }
        };

        SFuncFParam = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.takeWithType(TokenType.BType));
                derivatives.add(tokens.takeWithType(TokenType.Ident));
                if (tokens.cur().equals(SymbolToken.LBRACKSYM)) {
                    // 此处可能为缺省维度
                    // 正常维度：正常（常量表达式），缺括号（表达式后左中括号或逗号或右小括号（缺了则是左大括号））
                    // 缺省维度：正常（右括号），缺括号（直接左中括号或逗号或右小括号（缺了则是左大括号））
                    if (tokens.peek().equals(SymbolToken.RBRACKSYM)) {
                        // 缺省维度正常
                        derivatives.add(tokens.take(SymbolToken.LBRACKSYM));
                        derivatives.add(tokens.take(SymbolToken.RBRACKSYM));
                    } else if (tokens.peek().equals(SymbolToken.LBRACKSYM)
                            || tokens.peek().equals(SymbolToken.COMMASYM)
                            || tokens.peek().equals(SymbolToken.RPARENTSYM)
                            || tokens.peek().equals(SymbolToken.LBRACESYM)) {
                        // 缺省维度缺右中括号
                        derivatives.add(tokens.take(SymbolToken.LBRACKSYM));
                        try {
                            derivatives.add(tokens.take(SymbolToken.RBRACKSYM));
                        } catch (TakeTokenException e) {
                            derivatives.add(new TokenSyntax(SymbolToken.RBRACKSYM));
                            handler.save(e, ExceptionType.MissingRBrack, tokens.lastPosition());
                        }
                    }

                    // 正常维度
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
                }
                return new ParseSyntax(derivatives, SyntaxType.FuncFParam);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().isTypeOf(TokenType.BType) && tokens.peek(1).isTypeOf(TokenType.Ident);
            }
        };

        AFuncCall = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.takeWithType(TokenType.Ident));
                derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
                if (!tokens.cur().equals(SymbolToken.RPARENTSYM)) {
                    boolean success;
                    tokens.save();
                    try {
                        derivatives.add(SFuncRParams.parse(tokens, handler));
                        success = true;
                    } catch (FrontEndException e) {
                        // 无实参列表的缺右括号
                        success = false;
                    }
                    if (success) {
                        tokens.discard();
                    } else {
                        tokens.rewind();
                    }
                }
                try {
                    derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
                    handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
                }
                return new ParseSyntax(derivatives, SyntaxType.UnaryExp);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().isTypeOf(TokenType.Ident) && tokens.peek().equals(SymbolToken.LPARENTSYM);
            }
        };

        SFuncRParams = (tokens, handler) -> {
            List<MetaSyntax> derivatives = new ArrayList<>();
            derivatives.add(SExp.parse(tokens, handler));
            while (tokens.cur().equals(SymbolToken.COMMASYM)) {
                derivatives.add(tokens.take(SymbolToken.COMMASYM));
                derivatives.add(SExp.parse(tokens, handler));
            }
            return new ParseSyntax(derivatives, SyntaxType.FuncRParams);
        };
    }

}
