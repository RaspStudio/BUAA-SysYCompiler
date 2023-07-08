package frontend.token.meta;

import frontend.token.SourceIterator;
import util.Pair;

import java.util.Set;

public abstract class WordToken extends MetaToken {
    private static final String BIG_ALPHA = "ABCDEFG" + "HIJKLMN" + "OPQRST" + "UVWXYZ";
    private static final String SMALL_ALPHA = BIG_ALPHA.toLowerCase();
    private static final String DIGIT = "0123456789";
    private static final String UNDERLINE = "_";

    public static final String START_CHAR_SET = BIG_ALPHA + SMALL_ALPHA + UNDERLINE;
    public static final String MID_CHAR_SET = START_CHAR_SET + DIGIT;
    public static final String CHAR_SET = START_CHAR_SET + MID_CHAR_SET;

    protected WordToken(int line, int row, String content, Set<TokenType> types) {
        super(line, row, content, types);
    }

    public static boolean match(SourceIterator source) {
        return source.curIsIn(START_CHAR_SET);
    }

    public static WordToken parseWord(SourceIterator source) {
        Pair<Integer, Integer> pos = source.curPos();
        StringBuilder sb = new StringBuilder();
        while (source.curIsIn(MID_CHAR_SET)) {
            sb.append(source.cur());
            source.next();
        }

        if (KeyWordToken.match(sb.toString())) {
            return KeyWordToken.lexKeyWord(sb.toString(), pos.getLeft(), pos.getRight());
        } else {
            return IdentifierToken.lexIdent(sb.toString(), pos.getLeft(), pos.getRight());
        }
    }

}
