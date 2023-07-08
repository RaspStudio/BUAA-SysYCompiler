package frontend.tree.var;

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

public class VarDefNode extends MetaNode implements LabelNode<VarLabel> {
    private final String name;
    private final VarLabel label;
    private final InitValNode value;

    public VarDefNode(String name, VarLabel label, InitValNode val) {
        this.name = name;
        this.label = label;
        this.value = val;
    }

    @Override
    public VarLabel label() {
        return label;
    }

    public String name() {
        return name;
    }

    public boolean isUndefined() {
        return value.isUndefined();
    }

    public InitValNode getValue() {
        return value;
    }

    public static List<VarDefNode> treeify(ParseSyntax decl, LabelList labels, Handler handler) {
        final boolean isConst = decl.get(0).isType(SyntaxType.Wrapper)
                && decl.getToken(0).equals(KeyWordToken.CONSTTK);

        int i = isConst ? 1 : 0;
        final KeyWordToken base = (KeyWordToken) decl.getToken(i++);
        final List<VarDefNode> defs = new ArrayList<>();

        while (i < decl.size() - 1) {
            defs.add(treeifyDef((ParseSyntax) decl.get(i), base, isConst, labels, handler));
            i += 2;
        }

        return defs;
    }

    private static VarDefNode treeifyDef(ParseSyntax def, KeyWordToken base,
                                         boolean isConst, LabelList labels, Handler handler) {
        final String name = def.getToken(0).getContent();

        int i = 1;
        List<Integer> dimensions = new ArrayList<>();
        while (i < def.size() && def.get(i).isType(SyntaxType.Wrapper)
                && def.getToken(i).equals(SymbolToken.LBRACKSYM)) {
            // 匹配左中括号
            i++;

            // 匹配表达式
            if (def.get(i).isType(SyntaxType.ConstExp)) {
                dimensions.add(ExpNode.constTreeify((ParseSyntax) def.get(i), labels, handler));
                i++;
            } else {
                throw new RuntimeException("Unknown Situation");
            }

            if (def.get(i).isType(SyntaxType.Wrapper) && def.getToken(i).equals(SymbolToken.RBRACKSYM)) {
                i++;
            } else {
                throw new RuntimeException("Unknown Situation");
            }
        }

        InitValNode val;
        if (i < def.size() && def.get(i).isType(SyntaxType.Wrapper)
                && def.getToken(i).equals(SymbolToken.ASSIGNSYM)) {
            val = InitValNode.treeify((ParseSyntax) def.get(i + 1), isConst || labels.isTop(), labels, handler);
        } else if (!isConst) {
            val = InitValNode.undefined(dimensions);
        } else {
            throw new RuntimeException("Invalid Def");
        }

        if (!dimensions.equals(val.check())) {
            throw new RuntimeException("Invalid Init");
        }

        VarLabel label = new VarLabel(name, base, dimensions, val, isConst);

        try {
            labels.add(label);
        } catch (SyntaxException e) {
            handler.save(e, ExceptionType.DuplicatedIdent, def.getToken(0).getPos());
        }

        return new VarDefNode(name, label, val);
    }
}
