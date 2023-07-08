package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.SymbolToken;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class LOrExpNode {
    private final List<LAndExpNode> andExps;

    public LOrExpNode(List<LAndExpNode> andExps) {
        this.andExps = andExps;
    }

    public List<LAndExpNode> getAndExps() {
        return andExps;
    }

    public static LOrExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        Pair<List<ParseSyntax>, List<SymbolToken>> flat = ExpNode.flatten(syntax, SyntaxType.LOrExp);
        List<LAndExpNode> andExps = new ArrayList<>();
        flat.getLeft().forEach(o -> andExps.add(LAndExpNode.treeify(o, labels, handler)));
        return new LOrExpNode(andExps);
    }
}
