package frontend.tree.stmt;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.tree.MetaNode;

public abstract class StmtNode extends MetaNode {

    public static StmtNode treeify(ParseSyntax syntax, boolean isLoop, LabelList labels, Handler handler) {
        if (syntax.isType(SyntaxType.IfElseBranch)) {
            return BranchNode.treeify(syntax, isLoop, labels, handler);
        } else if (syntax.isType(SyntaxType.WhileLoop)) {
            return LoopNode.treeify(syntax, labels, handler);
        } else if (syntax.isType(SyntaxType.LoopCtrl)) {
            return LoopCtrlNode.treeify(syntax, isLoop, labels, handler);
        } else if (syntax.isType(SyntaxType.Return)) {
            return ReturnNode.treeify(syntax, labels, handler);
        } else if (syntax.isType(SyntaxType.Input)) {
            return InputNode.treeify(syntax, labels, handler);
        } else if (syntax.isType(SyntaxType.Output)) {
            return OutputNode.treeify(syntax, labels, handler);
        } else if (syntax.isType(SyntaxType.BlockStmt)) {
            return BlockNode.treeify((ParseSyntax) syntax.get(0), labels, handler, isLoop);
        } else if (syntax.isType(SyntaxType.Assign)) {
            return AssignNode.treeify(syntax, labels, handler);
        } else {
            return ExpStmtNode.treeify(syntax, labels, handler);
        }
    }

}
