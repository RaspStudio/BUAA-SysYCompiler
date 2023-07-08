package frontend.token.meta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static frontend.token.meta.TokenType.BType;
import static frontend.token.meta.TokenType.FuncType;
import static frontend.token.meta.TokenType.LoopCtrl;

public class KeyWordToken extends WordToken {
    private static final Map<String, String> CODE_MAP;
    private static final Map<String, KeyWordToken> TOKENS_MAP;

    /*---------- 初始化工具 ----------*/
    private KeyWordToken(int line, int row, String keyWord) {
        super(line, row, keyWord, TOKENS_MAP.get(keyWord).types);
    }

    private KeyWordToken(String keyWord, TokenType[] types) {
        super(0, 0, keyWord, new HashSet<>(Arrays.asList(types)));
    }

    private KeyWordToken(String keyWord) {
        super(0, 0, keyWord, new HashSet<>());
    }

    /*---------- 匹配工具 ----------*/
    public static boolean match(String s) {
        return CODE_MAP.containsKey(s);
    }

    public static KeyWordToken lexKeyWord(String s, int line, int row) {
        return new KeyWordToken(line, row, s);
    }

    /*---------- 字符化工具 ----------*/
    @Override
    protected String getTypeCode() {
        return CODE_MAP.get(content);
    }

    /*---------- 静态区 ----------*/
    public static final KeyWordToken MAINTK = new KeyWordToken("main");
    public static final KeyWordToken CONSTTK = new KeyWordToken("const");
    public static final KeyWordToken INTTK = new KeyWordToken("int", new TokenType[] { BType, FuncType });
    public static final KeyWordToken BREAKTK = new KeyWordToken("break", new TokenType[] { LoopCtrl });
    public static final KeyWordToken CONTINUETK = new KeyWordToken("continue", new TokenType[] { LoopCtrl });
    public static final KeyWordToken IFTK = new KeyWordToken("if");
    public static final KeyWordToken ELSETK = new KeyWordToken("else");
    public static final KeyWordToken WHILETK = new KeyWordToken("while");
    public static final KeyWordToken GETINTTK = new KeyWordToken("getint");
    public static final KeyWordToken PRINTFTK = new KeyWordToken("printf");
    public static final KeyWordToken RETURNTK = new KeyWordToken("return");
    public static final KeyWordToken VOIDTK = new KeyWordToken("void", new TokenType[] { FuncType });

    public static void init(KeyWordToken token, String code) {
        TOKENS_MAP.put(token.content, token);
        CODE_MAP.put(token.content, code);
    }

    static {
        CODE_MAP = new HashMap<>();
        TOKENS_MAP = new HashMap<>();

        init(MAINTK, "MAINTK");
        init(CONSTTK, "CONSTTK");
        init(INTTK, "INTTK");
        init(BREAKTK, "BREAKTK");
        init(CONTINUETK, "CONTINUETK");
        init(IFTK, "IFTK");
        init(ELSETK, "ELSETK");
        init(WHILETK, "WHILETK");
        init(GETINTTK, "GETINTTK");
        init(PRINTFTK, "PRINTFTK");
        init(RETURNTK, "RETURNTK");
        init(VOIDTK, "VOIDTK");
    }
}
