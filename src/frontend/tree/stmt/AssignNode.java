package frontend.tree.stmt;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.exp.ExpNode;
import frontend.tree.exp.LValNode;

public class AssignNode extends StmtNode {
    private final LValNode dest;
    private final ExpNode exp;

    public AssignNode(LValNode dest, ExpNode exp) {
        this.dest = dest;
        this.exp = exp;
    }

    public LValNode getDest() {
        return dest;
    }

    public ExpNode getExp() {
        return exp;
    }

    public static AssignNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        LValNode dest = LValNode.treeify((ParseSyntax) syntax.get(0), labels, handler);

        if (dest.label().isConst()) {
            handler.save(new SyntaxException("Const Assign Error"), ExceptionType.ConstModification, dest.pos());
        }

        return new AssignNode(
            dest,
            ExpNode.treeify((ParseSyntax) syntax.get(2), labels, handler)
        );
    }
}
