package frontend.tree.stmt;

import frontend.exception.ExceptionType;
import frontend.exception.Handler;
import frontend.exception.SyntaxException;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.StringToken;
import frontend.tree.exp.ExpNode;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class OutputNode extends StmtNode {
    private final StringToken formatString;
    private final List<ExpNode> exps;

    public OutputNode(StringToken formatString, List<ExpNode> exps) {
        this.formatString = formatString;
        this.exps = exps;
    }

    public StringToken getFormatString() {
        return formatString;
    }

    public List<ExpNode> getExps() {
        return exps;
    }

    public static OutputNode treeify(ParseSyntax syntax, LabelList labels, Handler handler) {
        StringToken token = (StringToken) syntax.getToken(2);
        List<ExpNode> exps = new ArrayList<>();
        int i = 4;
        while (i < syntax.size() && syntax.get(i).isType(SyntaxType.Exp)) {
            exps.add(ExpNode.treeify((ParseSyntax) syntax.get(i), labels, handler));
            i += 2;
        }

        Pair<Boolean, Integer> analysis = StringToken.analyze(token);
        if (!analysis.getLeft()) {
            handler.save(new SyntaxException("Invalid String"),
                    ExceptionType.InvalidString, token.getPos());
        }
        if (analysis.getRight() != exps.size()) {
            handler.save(new SyntaxException("Invalid Fmt"),
                    ExceptionType.InvalidOutputArgument, syntax.getToken(0).getPos());
        }

        return new OutputNode((StringToken) syntax.getToken(2), exps);
    }
}
