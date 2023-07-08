package frontend.syntax.factories;

import frontend.exception.Handler;
import frontend.exception.FrontEndException;
import frontend.syntax.TokenIterator;
import frontend.syntax.meta.MetaSyntax;

/**
 * 共 32 种非终结符，其中 3 个无标识符非终结符。
 * 共 29+3 种非终结符分布：
 *      6+1 种在 SVariable 中
 *      14+0 种在 SExpression 中
 *      6+0 种在 SFunction 中
 *      2+1 种在 SStatement 中
 *      0+1 种忽略 (BType)
 *      1+0 种顶层 (CompUnit)
 */
@FunctionalInterface
public interface SAbstract {
    /**
     * 匹配一个该非终结符，已知错误交给 handler 处理，其它错误抛出。
     * @param tokens 符号序列源
     * @param handler 已知错误处理器
     * @return 匹配到的非终结符，当内部没有错误时为合法非终结符，否则不保证内部结构
     */
    MetaSyntax parse(TokenIterator tokens, Handler handler) throws FrontEndException;

    /**
     * 查看符号序列是否符合该非终结符的特征。
     * @param tokens 符号序列源
     * @return 是否可以匹配该非终结符
     */
    default boolean match(TokenIterator tokens) {
        throw new RuntimeException("No Such Matching Function");
    }

    String toString();
}
