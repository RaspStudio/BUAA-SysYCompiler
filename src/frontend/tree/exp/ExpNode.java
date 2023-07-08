package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.label.meta.VarLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.SymbolToken;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExpNode extends MetaNode implements LabelNode<VarLabel> {
    private final AddExpNode exp;

    public ExpNode(AddExpNode exp) {
        this.exp = exp;
    }

    public AddExpNode getAddExp() {
        return exp;
    }

    @Override
    public VarLabel label() {
        return exp.label();
    }

    /*---------- 前端语义树构建 ----------*/
    public static ExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        return new ExpNode(AddExpNode.treeify((ParseSyntax) syntax.get(0), labels, handler));
    }

    public static Integer constTreeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        return AddExpNode.constTreeify((ParseSyntax) syntax.get(0), labels, handler);
    }

    /*---------- 表达式工具函数 ----------*/
    public static Pair<List<ParseSyntax>, List<SymbolToken>> flatten(ParseSyntax syntax, SyntaxType type) {
        ParseSyntax cur = syntax;
        LinkedList<ParseSyntax> syntaxes = new LinkedList<>();
        LinkedList<SymbolToken> tokens = new LinkedList<>();
        // 拆卸至最后
        while (cur.isType(type) && cur.size() > 1) {
            syntaxes.addFirst((ParseSyntax) cur.get(-1));
            tokens.addFirst((SymbolToken) TokenSyntax.getToken(cur.get(-2)));
            cur = (ParseSyntax) cur.get(0);
        }

        syntaxes.addFirst((ParseSyntax) cur.get(0));
        return new Pair<>(new ArrayList<>(syntaxes), new ArrayList<>(tokens));
    }
}
