package frontend.tree;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.function.FunctionNode;
import frontend.tree.var.VarDefNode;

import java.util.ArrayList;
import java.util.List;

public class CompUnitNode extends MetaNode {
    private final ParseSyntax topSyntax;
    private final List<VarDefNode> topVariables;
    private final List<FunctionNode> topFunctions;
    private final LabelList topLabelList;
    private final Handler handler;

    public CompUnitNode(ParseSyntax topSyntax, Handler handler) {
        this.topSyntax = topSyntax;
        this.handler = handler;
        this.topLabelList = LabelList.init();
        this.topVariables = new ArrayList<>();
        this.topFunctions = new ArrayList<>();
    }

    public List<VarDefNode> getTopVariables() {
        return new ArrayList<>(topVariables);
    }

    public List<FunctionNode> getTopFunctions() {
        return new ArrayList<>(topFunctions);
    }

    public void build() {
        if (topVariables.isEmpty() && topFunctions.isEmpty()) {
            for (int i = 0; i < topSyntax.size(); i++) {
                ParseSyntax syntax = (ParseSyntax) topSyntax.get(i);
                if (syntax.isType(SyntaxType.VarDecl) || syntax.isType(SyntaxType.ConstDecl)) {
                    topVariables.addAll(VarDefNode.treeify(syntax, topLabelList, handler));
                } else if (syntax.isType(SyntaxType.FuncDef)) {
                    topFunctions.add(FunctionNode.treeify(syntax, topLabelList, handler));
                } else if (syntax.isType(SyntaxType.MainFuncDef)) {
                    topFunctions.add(FunctionNode.treeifyMain(syntax, topLabelList, handler));
                } else {
                    throw new RuntimeException("Invalid CompUnit");
                }
            }
        }
    }
}
