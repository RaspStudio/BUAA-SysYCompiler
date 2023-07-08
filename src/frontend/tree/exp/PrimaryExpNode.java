package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.label.meta.VarLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.KeyWordToken;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import util.Pair;

import java.util.ArrayList;

public class PrimaryExpNode extends MetaNode implements LabelNode<VarLabel> {
    private final boolean isNumber;
    private final boolean isLVal;
    private final int number;
    private final LValNode leftVal;
    private final ExpNode exp;

    private PrimaryExpNode(int number) {
        this.isNumber = true;
        this.isLVal = false;
        this.number = number;
        this.leftVal = null;
        this.exp = null;
    }

    private PrimaryExpNode(LValNode leftVal) {
        this.isNumber = false;
        this.isLVal = true;
        this.number = 0;
        this.leftVal = leftVal;
        this.exp = null;
    }

    private PrimaryExpNode(ExpNode exp) {
        this.isNumber = false;
        this.isLVal = false;
        this.number = 0;
        this.leftVal = null;
        this.exp = exp;
    }

    public Pair<Integer, Pair<LValNode, ExpNode>> getContent() {
        return new Pair<>(isNumber ? number : null, new Pair<>(leftVal, exp));
    }

    @Override
    public VarLabel label() {
        if (isNumber) {
            return new VarLabel("LITERAL", KeyWordToken.INTTK, true, new ArrayList<>());
        } else {
            if (isLVal) {
                assert leftVal != null;
                return leftVal.label();
            } else {
                assert exp != null;
                return exp.label();
            }
        }
    }

    public static PrimaryExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        if (syntax.size() > 1) {
            return new PrimaryExpNode(ExpNode.treeify((ParseSyntax) syntax.get(1), labels, handler));
        }

        if (syntax.get(0).isType(SyntaxType.Number)) {
            ParseSyntax number = (ParseSyntax) syntax.get(0);
            return new PrimaryExpNode(Integer.parseInt(TokenSyntax.getToken(number.get(0)).getContent()));
        }

        if (syntax.get(0).isType(SyntaxType.LVal)) {
            return new PrimaryExpNode(LValNode.treeify((ParseSyntax) syntax.get(0), labels, handler));
        }

        throw new RuntimeException("Invalid PrimaryExp");
    }

    public static int constTreeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        if (syntax.size() > 1) {
            return ExpNode.constTreeify((ParseSyntax) syntax.get(1), labels, handler);
        }

        if (syntax.get(0).isType(SyntaxType.Number)) {
            ParseSyntax number = (ParseSyntax) syntax.get(0);
            return Integer.parseInt(TokenSyntax.getToken(number.get(0)).getContent());
        }

        if (syntax.get(0).isType(SyntaxType.LVal)) {
            return LValNode.constTreeify((ParseSyntax) syntax.get(0), labels, handler);
        }

        throw new RuntimeException("Invalid PrimaryExp");
    }
}
