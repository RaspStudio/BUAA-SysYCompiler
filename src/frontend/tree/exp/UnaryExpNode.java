package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.label.meta.VarLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.SymbolToken;
import frontend.token.meta.TokenType;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import frontend.tree.function.FuncCallNode;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UnaryExpNode extends MetaNode implements LabelNode<VarLabel> {
    private final boolean isFuncCall;
    private final List<ParseSyntax> ops;
    private final FuncCallNode funcCall;
    private final PrimaryExpNode exp;

    public UnaryExpNode(List<ParseSyntax> ops, FuncCallNode funcCall) {
        this.ops = ops;
        this.isFuncCall = true;
        this.funcCall = funcCall;
        this.exp = null;
    }

    public UnaryExpNode(List<ParseSyntax> ops, PrimaryExpNode exp) {
        this.ops = ops;
        this.isFuncCall = false;
        this.funcCall = null;
        this.exp = exp;
    }

    public Pair<Pair<String, Stack<Boolean>>, Pair<FuncCallNode, PrimaryExpNode>> getContent() {
        String lastSymbolVarName = null;
        Stack<Boolean> isNotOrNeg = new Stack<>();
        for (ParseSyntax op : ops) {
            SymbolToken token = (SymbolToken) op.getToken(0);
            if (token.equals(SymbolToken.MINUSYM)) {
                if (!isNotOrNeg.empty() && !isNotOrNeg.peek()) {
                    isNotOrNeg.pop();
                } else {
                    isNotOrNeg.push(false);
                }
                lastSymbolVarName = token.getVarName();
            } else if (token.equals(SymbolToken.PLUSSYM)) {
                lastSymbolVarName = token.getVarName();
            } else if (token.equals(SymbolToken.NOTSYM)) {
                isNotOrNeg.push(true);
                lastSymbolVarName = token.getVarName();
            }
        }
        return new Pair<>(new Pair<>(lastSymbolVarName, isNotOrNeg), new Pair<>(funcCall, exp));
    }

    @Override
    public VarLabel label() {
        if (isFuncCall) {
            assert funcCall != null;
            return funcCall.label();
        } else {
            assert exp != null;
            return exp.label();
        }
    }

    public static int constTreeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        List<ParseSyntax> ops = new ArrayList<>();

        ParseSyntax cur = syntax;
        while (cur.get(0).isType(SyntaxType.UnaryOp)) {
            ops.add((ParseSyntax) cur.get(0));
            cur = (ParseSyntax) cur.get(1);
        }

        boolean isPositive = true;

        for (ParseSyntax o : ops) {
            if (TokenSyntax.getToken(o.get(0)).getContent().equals("-")) {
                isPositive = !isPositive;
            }
        }

        if (cur.get(0).isType(SyntaxType.PrimaryExp)) {
            return PrimaryExpNode.constTreeify((ParseSyntax) cur.get(0), labels, handler) * (isPositive ? 1 : -1);
        }

        throw new RuntimeException("Invalid Const UnaryExp");
    }

    public static UnaryExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        List<ParseSyntax> ops = new ArrayList<>();

        ParseSyntax cur = syntax;
        while (cur.get(0).isType(SyntaxType.UnaryOp)) {
            ops.add((ParseSyntax) cur.get(0));
            cur = (ParseSyntax) cur.get(1);
        }

        if (cur.get(0).isType(SyntaxType.Wrapper)
                && TokenSyntax.getToken(cur.get(0)).isTypeOf(TokenType.Ident)
                && cur.get(1).isType(SyntaxType.Wrapper)
                && TokenSyntax.getToken(cur.get(1)).equals(SymbolToken.LPARENTSYM)) {
            return new UnaryExpNode(ops, FuncCallNode.treeify(cur, labels, handler));
        }

        if (cur.get(0).isType(SyntaxType.PrimaryExp)) {
            return new UnaryExpNode(ops, PrimaryExpNode.treeify((ParseSyntax) cur.get(0), labels, handler));
        }

        throw new RuntimeException("Invalid UnaryExp");
    }
}
