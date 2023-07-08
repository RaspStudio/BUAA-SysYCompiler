package frontend.token.meta;

import frontend.token.SourceIterator;
import util.Pair;

import java.util.Collections;
import java.util.HashSet;

public class StringToken extends MetaToken {
    private static final char DELIMITER = '\"';
    public static final String CHAR_SET = String.valueOf(DELIMITER);

    private StringToken(int line, int row, String keyWord) {
        super(line, row, keyWord, new HashSet<>(Collections.singletonList(TokenType.String)));
    }

    /*---------- 语义工具 ----------*/
    public static Pair<Boolean, Integer> analyze(StringToken token) {
        String removeNextLine = token.content.replace("\\n", "");
        String reserveSpecialSymbol = removeNextLine.replace("$", "#");
        String replaceSlash = reserveSpecialSymbol.replace("\\", "#");
        String formatToDollar = replaceSlash.replace("%d", "$");
        boolean isValid = true;
        int ret = 0;
        for (int i = 1; i < formatToDollar.length() - 1; i++) {
            char c = formatToDollar.charAt(i);
            if (c == '$') {
                ret += 1;
            } else if (c != ' ' && c != '!' && !(40 <= c && c <= 126)) {
                isValid = false;
            }
        }
        return new Pair<>(isValid, ret);
    }

    /*---------- 匹配工具 ----------*/
    public static boolean match(SourceIterator source) {
        return source.cur() == '\"';
    }

    public static StringToken lexStrConst(SourceIterator source) {
        final Pair<Integer, Integer> pos = source.curPos();
        StringBuilder sb = new StringBuilder();

        source.useCharSet(false); // 字符串内支持未知符号
        sb.append(source.cur()); // 拼接左双引号
        source.next(); // 跳过左双引号，返回第一个字符串内符号（可能未知）

        while (!source.curIsIn(DELIMITER + SourceIterator.STOP_STR)) {
            // 当 非字符串结尾 且 非文件末尾字符时 继续
            sb.append(source.cur());
            source.next();
        }

        // 退出循环时：字符串结尾 或 文件末尾字符
        source.useCharSet(true);

        if (source.curIsIn(SourceIterator.STOP_STR)) {
            throw new IllegalArgumentException("Missing Terminating \" For String");
        }
        sb.append(source.cur());
        source.next();// 返回“\"”后的第一个字符

        return new StringToken(pos.getLeft(), pos.getRight(), sb.toString());
    }

    /*---------- 字符化工具 ----------*/
    @Override
    protected String getTypeCode() {
        return "STRCON";
    }
}
