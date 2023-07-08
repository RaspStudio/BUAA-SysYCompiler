package frontend.token.meta;

import frontend.token.SourceIterator;
import util.Pair;

import java.util.Collections;
import java.util.HashSet;

public class IntegerToken extends MetaToken {
    public static final String CHAR_SET = "0123456789";

    private IntegerToken(int line, int row, String val) {
        super(line, row, val, new HashSet<>(Collections.singletonList(TokenType.Integer)));
    }

    /*---------- 求值工具 ----------*/
    public int value() {
        return Integer.parseInt(content);
    }

    /*---------- 匹配工具 ----------*/
    public static boolean match(SourceIterator source) {
        return source.curIsIn(CHAR_SET);
    }

    public static IntegerToken lexIntConst(SourceIterator source) {
        Pair<Integer, Integer> pos = source.curPos();
        StringBuilder sb = new StringBuilder();
        while (source.curIsIn(CHAR_SET)) {
            sb.append(source.cur());
            source.next();
        }
        return new IntegerToken(pos.getLeft(), pos.getRight(), sb.toString());
    }

    /*---------- 字符化工具 ----------*/
    @Override
    protected String getTypeCode() {
        return "INTCON";
    }
}
