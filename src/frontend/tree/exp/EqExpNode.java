package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.SymbolToken;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class EqExpNode {
    private final List<RelExpNode> relExps;
    private final List<SymbolToken> ops;

    public EqExpNode(List<RelExpNode> relExps, List<SymbolToken> ops) {
        this.relExps = relExps;
        this.ops = ops;
    }

    public List<RelExpNode> getRelExps() {
        return relExps;
    }

    public List<SymbolToken> getOps() {
        return ops;
    }

    public static EqExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        Pair<List<ParseSyntax>, List<SymbolToken>> flat = ExpNode.flatten(syntax, SyntaxType.EqExp);
        List<RelExpNode> andExps = new ArrayList<>();
        flat.getLeft().forEach(o -> andExps.add(RelExpNode.treeify(o, labels, handler)));
        return new EqExpNode(andExps, flat.getRight());
    }
}
