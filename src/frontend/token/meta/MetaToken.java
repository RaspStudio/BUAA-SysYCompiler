package frontend.token.meta;

import util.Pair;

import java.util.Objects;
import java.util.Set;

public abstract class MetaToken {
    // 符号的位置
    protected final int line;
    protected final int row;

    // 符号的原文
    protected final String content;

    // 符号的特征
    protected final Set<TokenType> types;

    /*---------- 初始化工具 ----------*/
    protected MetaToken(int line, int row, String content, Set<TokenType> types) {
        this.line = line;
        this.row = row;
        this.content = content;
        this.types = types;
    }

    /*---------- 判断工具 ----------*/
    public final boolean isTypeOf(TokenType type) {
        return types.contains(type);
    }

    /*---------- 文本化工具 ----------*/
    protected abstract String getTypeCode();

    public final String getContent() {
        return content;
    }

    public final String getTokenCode() {
        return getTypeCode() + " " + content;
    }

    public final Pair<Integer, Integer> getPos() {
        return new Pair<>(line, row);
    }

    @Override
    public final String toString() {
        return content + " (" + line + ", " + row + ")";
    }

    /*---------- 相等性、有序性工具 ----------*/
    @Override
    public final boolean equals(Object o) {
        return o instanceof MetaToken && ((MetaToken) o).content.equals(content);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(content);
    }
}
