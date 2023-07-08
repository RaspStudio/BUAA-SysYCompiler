package frontend.token;

import frontend.token.meta.IntegerToken;
import frontend.token.meta.MetaToken;
import frontend.token.meta.StringToken;
import frontend.token.meta.SymbolToken;
import frontend.token.meta.WordToken;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Lexer {
    // 数据源
    private final SourceIterator source;

    // 处理结果
    private final List<MetaToken> tokens;

    public Lexer(String source) {
        this.source = new SourceIterator(source, LEGAL_CHAR);
        this.tokens = new ArrayList<>();
    }

    /*---------- 公共结果接口 ----------*/
    public List<MetaToken> result() {
        if (tokens.isEmpty()) {
            analyze();
        }
        return new ArrayList<>(tokens);
    }

    /*---------- 工具区 ----------*/
    private void analyze() {
        while (!source.curIsIn(SourceIterator.STOP_STR)) {
            // 当前非文件结尾，继续分析单词

            while (source.curIsIn(BLANK)) {
                // 跳过空字符
                source.next();
            }

            // 当前为非空字符（NEXTLINE + IDENT_MID + SYMBOL + STRING + COMMENT）
            // 文件结尾时停止分析
            if (source.curIsIn(SourceIterator.STOP_STR)) {
                return;
            }

            if (source.curIsIn(SourceIterator.NEXTLINE)) {
                source.next();
                continue;
            }

            // 当前为非空字符且非结尾（IDENT_MID + SYMBOL + STRING + COMMENT）
            if (tryParseComment(source)) {
                // 尝试跳过注释
                continue;
            }

            // 四种有效字符的情况（Word/Integer/Symbol/String）
            if (IntegerToken.match(source)) {
                // 数字字符（1/4）
                tokens.add(IntegerToken.lexIntConst(source));
            } else if (StringToken.match(source)) {
                // 引号字符（2/4）
                tokens.add(StringToken.lexStrConst(source));
            } else if (WordToken.match(source)) {
                // 标识符字符（3/4）
                tokens.add(WordToken.parseWord(source));
            } else if (SymbolToken.match(source)) {
                // 符号字符（4/4）
                tokens.add(SymbolToken.lexSymbol(source));
            } else {
                // 非法字符的未知情况
                throw new RuntimeException("Unknown Situation Branch! " +
                        "Current Char is '" + source.cur() + "', " +
                        "On Line " + source.curPos().getLeft() +
                        ", Row " + source.curPos().getRight() + ".");
            }
        }
    }

    // 词法分析无关元素工具
    private static boolean tryParseComment(SourceIterator source) {
        // 跳过行注释
        if (source.curIsIn(COMMENT_EDGE) && source.nextIsIn(COMMENT_EDGE)) {
            source.useCharSet(false);
            // 下列循环第一次执行返回行注释的第二个“/”
            while (!source.curIsIn(SourceIterator.STOP_STR + SourceIterator.NEXTLINE)) {
                // 若当前字符 非换行字符 非文件末尾字符 则继续
                source.next();
            }
            // 此时当前字符为“\n” 或 文件结尾
            source.useCharSet(true);
            source.next(); // 跳过换行符或文件结尾不变
            return true;
        }

        // 跳过块注释
        if (source.curIsIn(COMMENT_EDGE) && source.nextIsIn(COMMENT_STAR)) {
            source.next();// 返回“*”
            source.useCharSet(false);
            source.next();// 返回注释符号后的第一个字符
            while ((!(source.curIsIn(COMMENT_STAR) && source.nextIsIn(COMMENT_EDGE)))
                    && !source.curIsIn(SourceIterator.STOP_STR)) {
                // 当 非块注释结尾 且 非文件末尾字符时 继续
                source.next();
            }
            // 当前为块注释结尾的“*”字符 或 文件末尾字符
            // 跳过“/*” 或 报错
            if (source.curIsIn(SourceIterator.STOP_STR)) {
                throw new IllegalArgumentException("Unterminated Block Comment!");
            }
            source.useCharSet(true);
            source.next();// 返回块注释的末尾“/”
            source.next();// 返回块注释后的第一个字符
            return true;
        }

        return false;
    }

    /*---------- 字符化区 ----------*/
    public String getTokenCode() {
        StringJoiner joiner = new StringJoiner("\n");
        result().forEach(o -> joiner.add(o.getTokenCode()));
        return joiner.toString();
    }

    /*---------- 静态区 ----------*/
    // 特殊符号
    private static final String BLANK = " \t\r";

    // 注释
    private static final String COMMENT_EDGE = "/";
    private static final String COMMENT_STAR = "*";
    private static final String COMMENT = COMMENT_STAR + COMMENT_EDGE;

    // 所有合法字符
    private static final String LEGAL_CHAR = BLANK + COMMENT +
            StringToken.CHAR_SET + WordToken.CHAR_SET + SymbolToken.CHAR_SET + IntegerToken.CHAR_SET;

}
