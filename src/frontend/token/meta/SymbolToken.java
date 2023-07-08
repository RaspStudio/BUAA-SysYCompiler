package frontend.token.meta;

import frontend.token.SourceIterator;
import util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class SymbolToken extends MetaToken {
    private static final Map<String, SymbolToken> TOKENS_MAP;
    private static final Map<String, String> CODE_MAP;
    public static final String CHAR_SET;

    /*---------- 初始化工具 ----------*/
    private SymbolToken(int line, int row, String symbol) {
        super(line, row, symbol, TOKENS_MAP.get(symbol).types);
    }

    private SymbolToken(String symbol, TokenType[] types) {
        super(0, 0, symbol, new HashSet<>(Arrays.asList(types)));
    }

    /*---------- 匹配工具 ----------*/
    public static boolean match(SourceIterator source) {
        char c1 = source.cur();
        char c2 = source.peek();
        String s1 = String.valueOf(c1);
        String s2 = c1 + String.valueOf(c2);
        return TOKENS_MAP.containsKey(s1) || TOKENS_MAP.containsKey(s2);
    }

    public static SymbolToken lexSymbol(SourceIterator source) {
        char c1 = source.cur();
        char c2 = source.peek();
        String s1 = String.valueOf(c1);
        String s2 = c1 + String.valueOf(c2);
        Pair<Integer, Integer> pos = source.curPos();
        if (TOKENS_MAP.containsKey(s2)) {
            source.next();
            source.next();
            return new SymbolToken(pos.getLeft(), pos.getRight(), s2);
        } else {
            source.next();
            return new SymbolToken(pos.getLeft(), pos.getRight(), s1);
        }
    }

    /*---------- 字符化工具 ----------*/
    @Override
    protected String getTypeCode() {
        return CODE_MAP.get(content);
    }

    public String getVarName() {
        return getTypeCode().toLowerCase(Locale.ROOT);
    }

    /*---------- 静态区 ----------*/
    // 逻辑运算符
    public static final SymbolToken NOTSYM = new SymbolToken("!", new TokenType[]{TokenType.UnaryOp});
    public static final SymbolToken ANDSYM = new SymbolToken("&&", new TokenType[]{});
    public static final SymbolToken ORSYM = new SymbolToken("||", new TokenType[]{});

    // 算术运算符
    public static final SymbolToken PLUSSYM = new SymbolToken("+", new TokenType[]{TokenType.AddOp, TokenType.UnaryOp});
    public static final SymbolToken MINUSYM = new SymbolToken("-", new TokenType[]{TokenType.AddOp, TokenType.UnaryOp});
    public static final SymbolToken MULTSYM = new SymbolToken("*", new TokenType[]{TokenType.MulOp});
    public static final SymbolToken DIVSYM = new SymbolToken("/", new TokenType[]{TokenType.MulOp});
    public static final SymbolToken MODSYM = new SymbolToken("%", new TokenType[]{TokenType.MulOp});

    // 比较运算符
    public static final SymbolToken LSSSYM = new SymbolToken("<", new TokenType[]{TokenType.RelOp});
    public static final SymbolToken LEQSYM = new SymbolToken("<=", new TokenType[]{TokenType.RelOp});
    public static final SymbolToken GRESYM = new SymbolToken(">", new TokenType[]{TokenType.RelOp});
    public static final SymbolToken GEQSYM = new SymbolToken(">=", new TokenType[]{TokenType.RelOp});
    public static final SymbolToken EQLSYM = new SymbolToken("==", new TokenType[]{TokenType.EqOp});
    public static final SymbolToken NEQSYM = new SymbolToken("!=", new TokenType[]{TokenType.EqOp});

    // 赋值运算符
    public static final SymbolToken ASSIGNSYM = new SymbolToken("=", new TokenType[]{});

    // 分隔符
    public static final SymbolToken SEMICNSYM = new SymbolToken(";", new TokenType[]{});
    public static final SymbolToken COMMASYM = new SymbolToken(",", new TokenType[]{});

    // 区域限制符
    public static final SymbolToken LPARENTSYM = new SymbolToken("(", new TokenType[]{});
    public static final SymbolToken RPARENTSYM = new SymbolToken(")", new TokenType[]{});
    public static final SymbolToken LBRACKSYM = new SymbolToken("[", new TokenType[]{});
    public static final SymbolToken RBRACKSYM = new SymbolToken("]", new TokenType[]{});
    public static final SymbolToken LBRACESYM = new SymbolToken("{", new TokenType[]{});
    public static final SymbolToken RBRACESYM = new SymbolToken("}", new TokenType[]{});

    public static void init(SymbolToken token, String code) {
        TOKENS_MAP.put(token.content, token);
        CODE_MAP.put(token.content, code);
    }

    static {
        TOKENS_MAP = new HashMap<>();
        CODE_MAP = new HashMap<>();

        // 逻辑运算符
        init(NOTSYM, "NOT");
        init(ANDSYM, "AND");
        init(ORSYM, "OR");

        // 算术运算符
        init(PLUSSYM, "PLUS");
        init(MINUSYM, "MINU");
        init(MULTSYM, "MULT");
        init(DIVSYM, "DIV");
        init(MODSYM, "MOD");

        // 比较运算符
        init(LSSSYM, "LSS");
        init(LEQSYM, "LEQ");
        init(GRESYM, "GRE");
        init(GEQSYM, "GEQ");
        init(EQLSYM, "EQL");
        init(NEQSYM, "NEQ");

        // 赋值运算符
        init(ASSIGNSYM, "ASSIGN");

        // 分隔符
        init(SEMICNSYM, "SEMICN");
        init(COMMASYM, "COMMA");

        // 区域限制符
        init(LPARENTSYM, "LPARENT");
        init(RPARENTSYM, "RPARENT");
        init(LBRACKSYM, "LBRACK");
        init(RBRACKSYM, "RBRACK");
        init(LBRACESYM, "LBRACE");
        init(RBRACESYM, "RBRACE");

        StringBuilder sb = new StringBuilder();
        TOKENS_MAP.keySet().forEach(sb::append);
        CHAR_SET = sb.toString();
    }
}
