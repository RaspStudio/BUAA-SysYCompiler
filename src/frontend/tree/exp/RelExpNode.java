package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.SymbolToken;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class RelExpNode {
    private final List<AddExpNode> addExps;
    private final List<SymbolToken> ops;

    public RelExpNode(List<AddExpNode> addExps, List<SymbolToken> ops) {
        this.addExps = addExps;
        this.ops = ops;
    }

    public List<AddExpNode> getAddExps() {
        return addExps;
    }

    public List<SymbolToken> getOps() {
        return ops;
    }

    public static RelExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        Pair<List<ParseSyntax>, List<SymbolToken>> flat = ExpNode.flatten(syntax, SyntaxType.RelExp);
        List<AddExpNode> addExps = new ArrayList<>();
        flat.getLeft().forEach(o -> addExps.add(AddExpNode.treeify(o, labels, handler)));
        return new RelExpNode(addExps, flat.getRight());
    }
}
