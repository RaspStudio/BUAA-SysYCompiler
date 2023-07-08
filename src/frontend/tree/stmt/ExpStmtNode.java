package frontend.tree.stmt;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.exp.ExpNode;

public class ExpStmtNode extends StmtNode {
    private final ExpNode exp;

    public ExpStmtNode() {
        exp = null;
    }

    public ExpStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public ExpNode getExp() {
        return exp;
    }

    public static ExpStmtNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        if (syntax.size() == 1) {
            return new ExpStmtNode();
        } else {
            return new ExpStmtNode(ExpNode.treeify((ParseSyntax) syntax.get(0), labels, handler));
        }
    }
}
