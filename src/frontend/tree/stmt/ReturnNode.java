package frontend.tree.stmt;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.label.meta.VarLabel;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.KeyWordToken;
import frontend.tree.Returnable;
import frontend.tree.LabelNode;
import frontend.tree.exp.ExpNode;
import util.Pair;

import java.util.ArrayList;

public class ReturnNode extends StmtNode implements LabelNode<VarLabel>, Returnable {
    private final boolean isVoid;
    private final KeyWordToken returnToken;
    private final ExpNode exp;
    private final VarLabel label;

    public ReturnNode(KeyWordToken returnToken) {
        this.isVoid = true;
        this.returnToken = returnToken;
        this.exp = null;
        this.label = new VarLabel("returnStmt", KeyWordToken.VOIDTK, true, new ArrayList<>());
    }

    public ReturnNode(KeyWordToken returnToken, ExpNode exp) {
        this.isVoid = false;
        this.returnToken = returnToken;
        this.exp = exp;
        this.label = exp.label();
    }

    public Pair<Boolean, ExpNode> getContent() {
        return new Pair<>(isVoid, exp);
    }

    public void checkVoidReturn(Handler handler) {
        VarLabel label = label();
        if (label.isBase(KeyWordToken.INTTK)) {
            handler.save(
                    new SyntaxException("Invalid Return Type"),
                    ExceptionType.InvalidFuncReturn,
                    getReturnToken().getPos()
            );
        }
    }

    public VarLabel label() {
        return label;
    }

    public KeyWordToken getReturnToken() {
        return returnToken;
    }

    public static ReturnNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        return syntax.size() == 2 ?
                new ReturnNode((KeyWordToken) syntax.getToken(0)) :
                new ReturnNode((KeyWordToken) syntax.getToken(0),
                        ExpNode.treeify((ParseSyntax) syntax.get(1), labels, handler));
    }
}
