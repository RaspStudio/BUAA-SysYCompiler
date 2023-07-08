package frontend.syntax;

import frontend.exception.TakeTokenException;
import frontend.syntax.meta.MetaSyntax;
import frontend.syntax.meta.TokenSyntax;
import frontend.token.meta.MetaToken;
import frontend.token.meta.TokenType;
import util.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class TokenIterator {
    // 基本功能
    private final List<MetaToken> tokens;
    private int curIndex;

    // 回溯功能
    private final Stack<Integer> rewinds;

    /*---------- 初始化器 ----------*/
    private TokenIterator(List<MetaToken> tokens) {
        this.tokens = tokens;
        this.curIndex = 0;
        this.rewinds = new Stack<>();
    }

    protected static TokenIterator init(List<MetaToken> tokens) {
        return new TokenIterator(tokens);
    }

    @Override
    public String toString() {
        return "Now: " + cur() + ", Next: " + peek();
    }

    /*---------- 高级功能接口 ----------*/
    public MetaSyntax take(MetaToken token) throws TakeTokenException {
        MetaToken cur = cur();
        if (Objects.equals(cur, token)) {
            next();
            return new TokenSyntax(cur);
        } else {
            throw new TakeTokenException(token, cur());
        }
    }

    public MetaSyntax takeWithType(TokenType type) throws TakeTokenException {
        MetaToken cur = cur();
        if (cur != null && cur.isTypeOf(type)) {
            next();
            return new TokenSyntax(cur);
        } else {
            throw new TakeTokenException(type, cur);
        }
    }

    public Pair<Integer, Integer> lastPosition() {
        return peek(-1).getPos();
    }

    /*---------- 一般功能接口 ----------*/
    public MetaToken cur() {
        return peek(0);
    }

    private void next() {
        curIndex = curIndex < tokens.size() ? curIndex + 1 : curIndex;
    }

    public MetaToken peek() {
        return peek(1);
    }

    public MetaToken peek(int offset) {
        int targetIndex = curIndex + offset;
        return targetIndex < tokens.size() ? tokens.get(targetIndex) : null;
    }

    /*---------- 回溯功能接口 ----------*/
    public void save() {
        rewinds.push(curIndex);
    }

    public void discard() {
        rewinds.pop();
    }

    public void rewind() {
        curIndex = rewinds.pop();
    }

}
