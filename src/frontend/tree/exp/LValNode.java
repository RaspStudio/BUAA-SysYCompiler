package frontend.tree.exp;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.label.meta.VarLabel;
import frontend.syntax.meta.ParseSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.IdentifierToken;
import frontend.token.meta.KeyWordToken;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import frontend.tree.PositionNode;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class LValNode extends MetaNode implements LabelNode<VarLabel>, PositionNode {
    private final String name;
    private final IdentifierToken ident;
    private final VarLabel label;
    private final List<ExpNode> dimensions;

    public LValNode(IdentifierToken ident, List<ExpNode> dimensions, VarLabel label) {
        this.name = ident.getContent();
        this.ident = ident;
        this.label = label;
        this.dimensions = dimensions;
    }

    public String name() {
        return name;
    }

    @Override
    public VarLabel label() {
        return label.subLabel(dimensions.size());
    }

    public VarLabel topLabel() {
        return label;
    }

    public List<ExpNode> getDimensions() {
        return dimensions;
    }

    @Override
    public Pair<Integer, Integer> pos() {
        return ident.getPos();
    }

    public Pair<KeyWordToken, List<ExpNode>> getType() {
        return new Pair<>(label.getType().getLeft(), dimensions);
    }

    public static int constTreeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        final String name = TokenSyntax.getToken(syntax.get(0)).getContent();
        List<Integer> exps = new ArrayList<>();

        for (int i = 2; i < syntax.size(); i += 3) {
            exps.add(ExpNode.constTreeify((ParseSyntax) syntax.get(i), labels, handler));
        }

        int ret;
        try {
            ret = labels.getValue(name, exps);
        } catch (SyntaxException e) {
            throw new IllegalArgumentException("Cannot Calculate!");
        }

        return ret;
    }

    public static LValNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        final IdentifierToken ident = (IdentifierToken) TokenSyntax.getToken(syntax.get(0));
        List<ExpNode> exps = new ArrayList<>();

        for (int i = 2; i < syntax.size(); i += 3) {
            exps.add(ExpNode.treeify((ParseSyntax) syntax.get(i), labels, handler));
        }


        VarLabel label = new VarLabel("Unknown", KeyWordToken.VOIDTK, true, new ArrayList<>());
        try {
            label = (VarLabel) labels.get(ident.getContent());
        } catch (SyntaxException e) {
            handler.save(new SyntaxException("Unknown Ident"),
                    ExceptionType.UnknownIdent, TokenSyntax.getToken(syntax.get(0)).getPos());
        }

        return new LValNode(ident, exps, label);
    }

}
