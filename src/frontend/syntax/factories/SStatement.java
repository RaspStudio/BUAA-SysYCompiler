package frontend.syntax.factories;

import frontend.exception.Handler;
import frontend.exception.ExceptionType;
import frontend.exception.FrontEndException;
import frontend.exception.TakeTokenException;
import frontend.syntax.TokenIterator;
import frontend.syntax.meta.MetaSyntax;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.KeyWordToken;
import frontend.token.meta.SymbolToken;
import frontend.token.meta.TokenType;

import java.util.ArrayList;
import java.util.List;

import static frontend.syntax.factories.SExpression.SCond;
import static frontend.syntax.factories.SExpression.SExp;
import static frontend.syntax.factories.SExpression.SLVal;
import static frontend.syntax.factories.SVariable.ADecl;

public class SStatement {
    public static final SAbstract SBlock;
    public static final SAbstract SStmt;
    public static final SAbstract AAssign;
    public static final SAbstract AExp;
    public static final SAbstract ABranch;
    public static final SAbstract ALoop;
    public static final SAbstract ALoopCtrl;
    public static final SAbstract AReturn;
    public static final SAbstract AInput;
    public static final SAbstract AOutput;
    public static final SAbstract ABlock;

    static {
        SBlock = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.take(SymbolToken.LBRACESYM));
                while (!tokens.cur().equals(SymbolToken.RBRACESYM)) {
                    if (ADecl.match(tokens)) {
                        derivatives.add(ADecl.parse(tokens, handler));
                    } else {
                        derivatives.add(SStmt.parse(tokens, handler));
                    }
                }
                derivatives.add(tokens.take(SymbolToken.RBRACESYM));
                return new ParseSyntax(derivatives, SyntaxType.Block);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().equals(SymbolToken.LBRACESYM);
            }
        };

        ABlock = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(SBlock.parse(tokens, handler));
                return new ParseSyntax(derivatives, SyntaxType.BlockStmt);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return SBlock.match(tokens);
            }
        };

        SStmt = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                if (ABranch.match(tokens)) {
                    return ABranch.parse(tokens, handler);
                } else if (ALoop.match(tokens)) {
                    return ALoop.parse(tokens, handler);
                } else if (ALoopCtrl.match(tokens)) {
                    return ALoopCtrl.parse(tokens, handler);
                } else if (AReturn.match(tokens)) {
                    return AReturn.parse(tokens, handler);
                } else if (AOutput.match(tokens)) {
                    return AOutput.parse(tokens, handler);
                } else if (ABlock.match(tokens)) {
                    return ABlock.parse(tokens, handler);
                } else {
                    if (tokens.cur().isTypeOf(TokenType.Ident)) {
                        tokens.save();
                        SLVal.parse(tokens, handler);
                        if (tokens.cur().equals(SymbolToken.ASSIGNSYM)) {
                            tokens.take(SymbolToken.ASSIGNSYM);
                            if (tokens.cur().equals(KeyWordToken.GETINTTK)) {
                                tokens.rewind();
                                return AInput.parse(tokens, handler);
                            } else {
                                tokens.rewind();
                                return AAssign.parse(tokens, handler);
                            }
                        } else {
                            tokens.rewind();
                        }
                    }

                    return AExp.parse(tokens, handler);
                }
            }
        };

        AAssign = (tokens, handler) -> {
            List<MetaSyntax> derivatives = new ArrayList<>();
            derivatives.add(SLVal.parse(tokens, handler));
            derivatives.add(tokens.take(SymbolToken.ASSIGNSYM));
            derivatives.add(SExp.parse(tokens, handler));
            try {
                derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
            } catch (TakeTokenException e) {
                derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
            }
            return new ParseSyntax(derivatives, SyntaxType.Assign);
        };

        AExp = (tokens, handler) -> {
            List<MetaSyntax> derivatives = new ArrayList<>();
            if (!tokens.cur().equals(SymbolToken.SEMICNSYM)) {
                // 不存在 没有表达式 且 缺分号 的情况（则语句没有元素）
                derivatives.add(SExp.parse(tokens, handler));
            }
            try {
                derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
            } catch (TakeTokenException e) {
                derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
            }
            return new ParseSyntax(derivatives, SyntaxType.ExpStmt);
        };

        ABranch = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.take(KeyWordToken.IFTK));
                derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
                derivatives.add(SCond.parse(tokens, handler));
                try {
                    derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
                    handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
                }
                derivatives.add(SStmt.parse(tokens, handler));
                if (tokens.cur().equals(KeyWordToken.ELSETK)) {
                    derivatives.add(tokens.take(KeyWordToken.ELSETK));
                    derivatives.add(SStmt.parse(tokens, handler));
                }

                return new ParseSyntax(derivatives, SyntaxType.IfElseBranch);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().equals(KeyWordToken.IFTK);
            }
        };

        ALoop = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.take(KeyWordToken.WHILETK));
                derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
                derivatives.add(SCond.parse(tokens, handler));
                try {
                    derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
                    handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
                }
                derivatives.add(SStmt.parse(tokens, handler));

                return new ParseSyntax(derivatives, SyntaxType.WhileLoop);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().equals(KeyWordToken.WHILETK);
            }
        };

        ALoopCtrl = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.takeWithType(TokenType.LoopCtrl));
                try {
                    derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                    handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
                }
                return new ParseSyntax(derivatives, SyntaxType.LoopCtrl);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().isTypeOf(TokenType.LoopCtrl);
            }
        };

        AReturn = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.take(KeyWordToken.RETURNTK));

                if (!tokens.cur().equals(SymbolToken.SEMICNSYM)) {
                    // 尝试 有表达式 或 无表达式缺分号
                    tokens.save();
                    boolean success;
                    try {
                        derivatives.add(SExp.parse(tokens, handler));
                        success = true;
                    } catch (FrontEndException e) {
                        success = false;
                    }
                    if (success) {
                        tokens.discard();
                    } else {
                        tokens.rewind();
                    }
                }

                try {
                    derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                    handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
                }
                return new ParseSyntax(derivatives, SyntaxType.Return);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().equals(KeyWordToken.RETURNTK);
            }
        };

        AOutput = new SAbstract() {
            @Override
            public MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException {
                List<MetaSyntax> derivatives = new ArrayList<>();
                derivatives.add(tokens.take(KeyWordToken.PRINTFTK));
                derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
                derivatives.add(tokens.takeWithType(TokenType.String));
                while (tokens.cur().equals(SymbolToken.COMMASYM)) {
                    derivatives.add(tokens.take(SymbolToken.COMMASYM));
                    derivatives.add(SExp.parse(tokens, handler));
                }
                try {
                    derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
                    handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
                }
                try {
                    derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
                } catch (TakeTokenException e) {
                    derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                    handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
                }
                return new ParseSyntax(derivatives, SyntaxType.Output);
            }

            @Override
            public boolean match(TokenIterator tokens) {
                return tokens.cur().equals(KeyWordToken.PRINTFTK);
            }
        };

        AInput = (tokens, handler) -> {
            List<MetaSyntax> derivatives = new ArrayList<>();
            derivatives.add(SLVal.parse(tokens, handler));
            derivatives.add(tokens.take(SymbolToken.ASSIGNSYM));
            derivatives.add(tokens.take(KeyWordToken.GETINTTK));
            derivatives.add(tokens.take(SymbolToken.LPARENTSYM));
            try {
                derivatives.add(tokens.take(SymbolToken.RPARENTSYM));
            } catch (TakeTokenException e) {
                derivatives.add(new TokenSyntax(SymbolToken.RPARENTSYM));
                handler.save(e, ExceptionType.MissingRParent, tokens.lastPosition());
            }
            try {
                derivatives.add(tokens.take(SymbolToken.SEMICNSYM));
            } catch (TakeTokenException e) {
                derivatives.add(new TokenSyntax(SymbolToken.SEMICNSYM));
                handler.save(e, ExceptionType.MissingSemiColon, tokens.lastPosition());
            }
            return new ParseSyntax(derivatives, SyntaxType.Input);
        };
    }

}
