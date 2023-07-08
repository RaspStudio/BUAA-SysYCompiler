package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;

public class CondNode {
    private final LOrExpNode orExp;

    public CondNode(LOrExpNode orExp) {
        this.orExp = orExp;
    }

    public LOrExpNode getOrExp() {
        return orExp;
    }

    public static CondNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        return new CondNode(LOrExpNode.treeify((ParseSyntax) syntax.get(0), labels, handler));
    }
}
