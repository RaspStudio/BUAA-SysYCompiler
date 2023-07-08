package frontend.tree.stmt;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.Returnable;
import frontend.tree.exp.CondNode;

public class LoopNode extends StmtNode implements Returnable {
    private final CondNode cond;
    private final StmtNode stmt;

    public LoopNode(CondNode cond, StmtNode stmt) {
        this.cond = cond;
        this.stmt = stmt;
    }

    public CondNode getCond() {
        return cond;
    }

    public BlockNode getStmt() {
        return BranchNode.wrap(stmt);
    }

    @Override
    public void checkVoidReturn(Handler handler) {
        if (stmt instanceof Returnable) {
            ((Returnable) stmt).checkVoidReturn(handler);
        }
    }

    public static LoopNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        return new LoopNode(
                CondNode.treeify((ParseSyntax) syntax.get(2), labels, handler),
                treeify((ParseSyntax) syntax.get(4), true, labels, handler)
        );
    }
}
