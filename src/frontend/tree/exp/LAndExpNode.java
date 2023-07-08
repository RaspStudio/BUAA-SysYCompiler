package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.SymbolToken;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class LAndExpNode {
    private final List<EqExpNode> eqExps;

    public LAndExpNode(List<EqExpNode> eqExps) {
        this.eqExps = eqExps;
    }

    public List<EqExpNode> getEqExps() {
        return eqExps;
    }

    public static LAndExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        Pair<List<ParseSyntax>, List<SymbolToken>> flat = ExpNode.flatten(syntax, SyntaxType.LAndExp);
        List<EqExpNode> eqExps = new ArrayList<>();
        flat.getLeft().forEach(o -> eqExps.add(EqExpNode.treeify(o, labels, handler)));
        return new LAndExpNode(eqExps);
    }
}
