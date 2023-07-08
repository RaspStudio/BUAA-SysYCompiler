package frontend.tree.stmt;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.label.meta.FunctionLabel;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.KeyWordToken;
import frontend.tree.MetaNode;
import frontend.tree.Returnable;
import frontend.tree.var.VarDefNode;

import java.util.ArrayList;
import java.util.List;

public class BlockNode extends StmtNode implements Returnable {
    private final List<VarDefNode> vars;
    private final List<StmtNode> stmts;
    private final List<MetaNode> varsAndStmts;

    public BlockNode(List<VarDefNode> vars, List<StmtNode> stmts, List<MetaNode> varsAndStmts) {
        this.vars = vars;
        this.stmts = stmts;
        this.varsAndStmts = varsAndStmts;
    }

    public List<MetaNode> getContent() {
        return new ArrayList<>(varsAndStmts);
    }

    @Override
    public void checkVoidReturn(Handler handler) {
        for (StmtNode o : stmts) {
            if (o instanceof Returnable) {
                ((Returnable) o).checkVoidReturn(handler);
            }
        }
    }

    public static BlockNode treeify(ParseSyntax syntax, LabelList labels, Handler handler, boolean isLoop) {
        List<VarDefNode> vars = new ArrayList<>();
        List<StmtNode> stmts = new ArrayList<>();
        List<MetaNode> varsAndStmts = new ArrayList<>();
        LabelList subList = labels.derive();
        for (int i = 1; i < syntax.size() - 1; i++) {
            if (syntax.get(i).isType(SyntaxType.VarDecl) || syntax.get(i).isType(SyntaxType.ConstDecl)) {
                List<VarDefNode> ret = VarDefNode.treeify((ParseSyntax) syntax.get(i), subList, handler);
                vars.addAll(ret);
                varsAndStmts.addAll(ret);
            } else {
                StmtNode ret = treeify((ParseSyntax) syntax.get(i), isLoop, subList, handler);
                stmts.add(ret);
                varsAndStmts.add(ret);
            }
        }
        return new BlockNode(vars, stmts, varsAndStmts);
    }

    public static BlockNode treeifyFunc(ParseSyntax syntax, LabelList labels, Handler handler, FunctionLabel func) {
        List<VarDefNode> vars = new ArrayList<>();
        List<StmtNode> stmts = new ArrayList<>();
        List<MetaNode> varsAndStmts = new ArrayList<>();
        for (int i = 1; i < syntax.size() - 1; i++) {
            if (syntax.get(i).isType(SyntaxType.VarDecl) || syntax.get(i).isType(SyntaxType.ConstDecl)) {
                List<VarDefNode> ret = VarDefNode.treeify((ParseSyntax) syntax.get(i), labels, handler);
                vars.addAll(ret);
                varsAndStmts.addAll(ret);
            } else {
                StmtNode ret = treeify((ParseSyntax) syntax.get(i), false, labels, handler);
                stmts.add(ret);
                varsAndStmts.add(ret);
            }
        }

        BlockNode current = new BlockNode(vars, stmts, varsAndStmts);

        if (func.isBase(KeyWordToken.VOIDTK)) {
            current.checkVoidReturn(handler);
        } else if (stmts.size() == 0 || !(stmts.get(stmts.size() - 1) instanceof ReturnNode)) {
            handler.save(
                    new SyntaxException("Missing Return"),
                    ExceptionType.FuncNeedReturn,
                    syntax.getToken(-1).getPos()
            );
        }

        return current;
    }
}
