package frontend.tree.stmt;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.exp.LValNode;

public class InputNode extends StmtNode {
    private final LValNode dest;

    public InputNode(LValNode dest) {
        this.dest = dest;
    }

    public LValNode getDest() {
        return dest;
    }

    public static InputNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        return new InputNode(LValNode.treeify((ParseSyntax) syntax.get(0), labels, handler));
    }
}
