package frontend.tree.function;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.label.meta.VarLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.KeyWordToken;
import frontend.token.meta.SymbolToken;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import frontend.tree.exp.ExpNode;

import java.util.ArrayList;
import java.util.List;

public class FuncFParamNode extends MetaNode implements LabelNode<VarLabel> {
    private final String name;
    private final VarLabel label;

    public FuncFParamNode(String name, VarLabel label) {
        this.label = label;
        this.name = name;
    }

    @Override
    public VarLabel label() {
        return label;
    }

    public String getName() {
        return name;
    }

    public static List<FuncFParamNode> treeify(ParseSyntax params, LabelList labels, Handler handler) {
        List<FuncFParamNode> nodes = new ArrayList<>();
        for (int i = 0; i * 2 < params.size(); i++) {
            assert params.get(i * 2).isType(SyntaxType.FuncFParam);
            nodes.add(treeifyNode((ParseSyntax) params.get(i * 2), labels, handler));
        }
        return nodes;
    }

    private static FuncFParamNode treeifyNode(ParseSyntax param, LabelList labels, Handler handler) {
        KeyWordToken base = (KeyWordToken) param.getToken(0);
        String name = param.getToken(1).getContent();

        int i = 2;
        List<Integer> dimensions = new ArrayList<>();
        while (i < param.size() && param.get(i).isType(SyntaxType.Wrapper)
                && param.getToken(i).equals(SymbolToken.LBRACKSYM)) {
            // 匹配左中括号
            i++;

            // 匹配表达式
            if (param.get(i).isType(SyntaxType.ConstExp)) {
                dimensions.add(ExpNode.constTreeify((ParseSyntax) param.get(i), labels, handler));
                i++;
            }

            if (param.get(i).isType(SyntaxType.Wrapper) && param.getToken(i).equals(SymbolToken.RBRACKSYM)) {
                if (i == 3) {
                    dimensions.add(null);
                }
                i++;
            } else {
                throw new RuntimeException("Unknown Situation");
            }
        }

        VarLabel label = new VarLabel(name, base, dimensions);

        try {
            labels.add(label);
        } catch (SyntaxException e) {
            handler.save(e, ExceptionType.DuplicatedIdent, param.getToken(1).getPos());
        }

        return new FuncFParamNode(name, label);
    }

}
