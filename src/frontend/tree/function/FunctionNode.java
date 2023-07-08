package frontend.tree.function;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.label.meta.FunctionLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.KeyWordToken;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import frontend.tree.stmt.BlockNode;

import java.util.ArrayList;
import java.util.List;

public class FunctionNode extends MetaNode implements LabelNode<FunctionLabel> {
    private final KeyWordToken type;
    private final String name;
    private final List<FuncFParamNode> fakeParams;
    private final BlockNode block;
    private final FunctionLabel label;

    private FunctionNode(KeyWordToken type, String name, List<FuncFParamNode> fakeParams, BlockNode block) {
        this.type = type;
        this.name = name;
        this.fakeParams = fakeParams;
        this.block = block;
        this.label = new FunctionLabel(name, type, fakeParams);
    }

    public BlockNode getBlock() {
        return block;
    }

    public List<FuncFParamNode> getFakeParams() {
        return fakeParams;
    }

    public String name() {
        return name;
    }

    @Override
    public FunctionLabel label() {
        return label;
    }

    public KeyWordToken getType() {
        return type;
    }

    public static FunctionNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        final ParseSyntax funcType = (ParseSyntax) syntax.get(0);
        final KeyWordToken type = (KeyWordToken) funcType.getToken(0);
        final String name = syntax.getToken(1).getContent();

        return getFunctionNode(syntax, labels, handler, type, name);
    }

    public static FunctionNode treeifyMain(ParseSyntax syntax, LabelList labels, Handler handler) {
        final KeyWordToken type = (KeyWordToken) syntax.getToken(0);
        final String name = KeyWordToken.MAINTK.getContent();

        return getFunctionNode(syntax, labels, handler, type, name);
    }

    private static FunctionNode getFunctionNode(ParseSyntax syntax, LabelList labels, Handler handler,
                                                KeyWordToken type, String name) {
        final LabelList subLabels = labels.derive();

        List<FuncFParamNode> fakeParams;
        if (syntax.get(3).isType(SyntaxType.FuncFParams)) {
            fakeParams = FuncFParamNode.treeify((ParseSyntax) syntax.get(3), subLabels, handler);
        } else {
            fakeParams = new ArrayList<>();
        }

        FunctionLabel label = new FunctionLabel(name, type, fakeParams);
        try {
            labels.add(label);
        } catch (SyntaxException e) {
            handler.save(e, ExceptionType.DuplicatedIdent, syntax.getToken(1).getPos());
        }

        BlockNode block = BlockNode.treeifyFunc((ParseSyntax) syntax.get(-1), subLabels, handler, label);

        return new FunctionNode(type, name, fakeParams, block);
    }

}
