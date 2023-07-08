package frontend.tree.exp;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.label.meta.Label;
import frontend.label.meta.VarLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.SymbolToken;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MulExpNode extends MetaNode implements LabelNode<VarLabel> {
    private final List<UnaryExpNode> exps;
    private final List<SymbolToken> ops;

    public MulExpNode(List<UnaryExpNode> exps, List<SymbolToken> ops) {
        this.exps = exps;
        this.ops = ops;
    }

    public Pair<List<UnaryExpNode>, List<SymbolToken>> getMulExps() {
        return new Pair<>(exps, ops);
    }

    @Override
    public VarLabel label() {
        return Label.deriveLabel(exps);
    }

    public static MulExpNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        Pair<List<ParseSyntax>, List<SymbolToken>> flat = ExpNode.flatten(syntax, SyntaxType.MulExp);
        List<UnaryExpNode> exps = new ArrayList<>();
        List<SymbolToken> ops = flat.getRight();
        flat.getLeft().forEach(o -> exps.add(UnaryExpNode.treeify(o, labels, handler)));
        return new MulExpNode(exps, ops);
    }

    public static int constTreeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        Pair<List<ParseSyntax>, List<SymbolToken>> flat = ExpNode.flatten(syntax, SyntaxType.MulExp);
        List<ParseSyntax> exps = flat.getLeft();
        List<SymbolToken> ops = flat.getRight();

        int ret = UnaryExpNode.constTreeify(exps.get(0), labels, handler);
        for (int i = 0; i < ops.size(); i++) {
            switch (ops.get(i).getContent()) {
                case "*":
                    ret *= UnaryExpNode.constTreeify(exps.get(i + 1), labels, handler);
                    break;
                case "/":
                    ret /= UnaryExpNode.constTreeify(exps.get(i + 1), labels, handler);
                    break;
                case "%":
                    ret %= UnaryExpNode.constTreeify(exps.get(i + 1), labels, handler);
                    break;
                default:
                    throw new RuntimeException("Invalid Op of MulExp");
            }
        }
        return ret;
    }
}
