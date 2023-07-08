package frontend.tree.function;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.label.meta.FunctionLabel;
import frontend.label.meta.VarLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.exp.ExpNode;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;

import java.util.ArrayList;
import java.util.List;

public class FuncCallNode extends MetaNode implements LabelNode<VarLabel> {
    private final String name;
    private final List<ExpNode> rparams;
    private final FunctionLabel funcLabel;

    public FuncCallNode(String name, List<ExpNode> rparams, FunctionLabel funcLabel) {
        this.name = name;
        this.rparams = rparams;
        this.funcLabel = funcLabel;
    }

    public String name() {
        return name;
    }

    public List<ExpNode> getRparams() {
        return rparams;
    }

    @Override
    public VarLabel label() {
        return funcLabel.returnLabel();
    }

    public static FuncCallNode treeify(ParseSyntax cur, LabelList labels, Handler handler) {
        final String name = cur.getToken(0).getContent();
        final List<ExpNode> rparams = new ArrayList<>();

        if (cur.get(2).isType(SyntaxType.FuncRParams)) {
            ParseSyntax rparamsSyntax = (ParseSyntax) cur.get(2);
            for (int i = 0; i < rparamsSyntax.size(); i += 2) {
                rparams.add(ExpNode.treeify((ParseSyntax) rparamsSyntax.get(i), labels, handler));
            }
        }

        FunctionLabel label;
        if (!labels.has(name, true)) {
            handler.save(new SyntaxException("Unknown Function"),
                    ExceptionType.UnknownIdent, cur.getToken(0).getPos());
            label = null;
        } else {
            try {
                label = (FunctionLabel) labels.get(name);
            } catch (SyntaxException e) {
                throw new RuntimeException("Unknown Situation");
            }
            if (label.size() != rparams.size()) {
                handler.save(new SyntaxException("Function Params Number Error"),
                        ExceptionType.InvalidRParamNumber, cur.getToken(0).getPos());
            } else {
                List<VarLabel> paramLabels = new ArrayList<>();
                for (ExpNode e : rparams) {
                    paramLabels.add(e.label());
                }
                if (!label.isValid(paramLabels)) {
                    handler.save(new SyntaxException("Function Params Number Error"),
                            ExceptionType.InvalidRParamType, cur.getToken(0).getPos());
                }
            }
        }

        return new FuncCallNode(name, rparams, label);
    }
}
