package frontend.token.meta;

import java.util.Collections;
import java.util.HashSet;

public class IdentifierToken extends WordToken {
    private IdentifierToken(int line, int row, String keyWord) {
        super(line, row, keyWord, new HashSet<>(Collections.singletonList(TokenType.Ident)));
    }

    /*---------- 匹配工具 ----------*/
    public static IdentifierToken lexIdent(String s, int line, int row) {
        return new IdentifierToken(line, row, s);
    }

    /*---------- 字符化工具 ----------*/
    @Override
    protected String getTypeCode() {
        return "IDENFR";
    }
}
