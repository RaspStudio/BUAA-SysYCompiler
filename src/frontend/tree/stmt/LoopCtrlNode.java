package frontend.tree.stmt;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.KeyWordToken;

public class LoopCtrlNode extends StmtNode {
    private final KeyWordToken ctrl;

    public LoopCtrlNode(KeyWordToken ctrl) {
        this.ctrl = ctrl;
    }

    public KeyWordToken getCtrl() {
        return ctrl;
    }

    public static LoopCtrlNode treeify(ParseSyntax parseSyntax, boolean isLoop, LabelList labels, Handler handler) {
        KeyWordToken token = (KeyWordToken) parseSyntax.getToken(0);
        if (!isLoop) {
            handler.save(new SyntaxException("Loop Ctrl Invalid"), ExceptionType.InvalidLoopCtrl, token.getPos());
        }
        return new LoopCtrlNode(token);
    }
}
