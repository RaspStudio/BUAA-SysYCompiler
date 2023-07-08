package frontend.tree.stmt;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.Returnable;
import frontend.tree.exp.CondNode;

import java.util.Collections;

public class BranchNode extends StmtNode implements Returnable {
    private final CondNode cond;
    private final StmtNode ifStmt;
    private final StmtNode elseStmt;

    public BranchNode(CondNode cond, StmtNode ifStmt, StmtNode elseStmt) {
        this.cond = cond;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
    }

    public CondNode getCond() {
        return cond;
    }

    protected static BlockNode wrap(StmtNode node) {
        if (node instanceof BlockNode) {
            return (BlockNode) node;
        } else {
            return new BlockNode(Collections.emptyList(),
                    Collections.singletonList(node), Collections.singletonList(node));
        }
    }

    public BlockNode getIfStmt() {
        return wrap(ifStmt);
    }

    public BlockNode getElseStmt() {
        return elseStmt == null ? null : wrap(elseStmt);
    }

    @Override
    public void checkVoidReturn(Handler handler) {
        if (ifStmt instanceof Returnable) {
            ((Returnable) ifStmt).checkVoidReturn(handler);
        }

        if (elseStmt instanceof Returnable) {
            ((Returnable) elseStmt).checkVoidReturn(handler);
        }
    }

    public static BranchNode treeify(ParseSyntax syntax, boolean isLoop, LabelList labels, Handler handler) {
        return new BranchNode(
                CondNode.treeify((ParseSyntax) syntax.get(2), labels, handler),
                StmtNode.treeify((ParseSyntax) syntax.get(4), isLoop, labels, handler),
                syntax.size() == 7 ?
                        StmtNode.treeify((ParseSyntax) syntax.get(6), isLoop, labels, handler) :
                        null
        );
    }
}
