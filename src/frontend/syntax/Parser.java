package frontend.syntax;

import frontend.exception.FrontEndException;
import frontend.exception.Handler;
import frontend.syntax.factories.SCompUnit;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.MetaToken;
import frontend.tree.CompUnitNode;

import java.util.List;

public class Parser {
    private final TokenIterator source;
    private final Handler handler;
    private ParseSyntax topSyntax;
    private CompUnitNode node;

    public Parser(List<MetaToken> source) {
        this.source = TokenIterator.init(source);
        this.handler = new Handler();
        this.topSyntax = null;
        node = null;
    }

    /*---------- 私有流程工具 ----------*/
    private ParseSyntax getTopSyntax() throws FrontEndException {
        if (topSyntax == null) {
            topSyntax = (ParseSyntax) SCompUnit.SCompUnit.parse(source, handler);
        }
        return topSyntax;
    }

    private CompUnitNode getTree() throws FrontEndException {
        if (node == null) {
            node = new CompUnitNode(getTopSyntax(), handler);
            node.build();
        }
        return node;
    }

    /*---------- 公共文本化接口 ----------*/
    public String getTokenCode() throws FrontEndException {
        return getTopSyntax().getTokenCode();
    }

    public String getExceptionCode() throws FrontEndException {
        getTree();
        return handler.getExceptionCode();
    }

    public CompUnitNode getRoot() {
        try {
            return getTree();
        } catch (FrontEndException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot Get Root When Parser Fails!");
        }
    }

    public boolean hasException() {
        return handler.hasException();
    }
}
