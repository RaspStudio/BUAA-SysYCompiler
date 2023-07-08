package frontend.token;

import util.Pair;

public class SourceIterator {
    private final String source;
    private final String charSet;
    private int curLine;
    private int curRow;
    private int curIndex;
    private boolean ignoreCharSet;

    private static final int TAB_SPACE = 4;
    private static final char TAB = '\t';
    public static final char STOP = '\0';
    private static final String TAB_STR = String.valueOf(TAB);
    public static final String STOP_STR = String.valueOf(STOP);
    public static final String NEXTLINE = "\n";

    protected SourceIterator(String source, String charSet) {
        this.source = source;
        this.charSet = charSet + TAB_STR + STOP_STR + NEXTLINE;
        curLine = 1;
        curRow = 1;
        curIndex = 0;
        ignoreCharSet = false;
    }

    /*----- 公共接口函数 -----*/
    public char cur() {
        return at(curIndex);
    }

    public char peek() {
        return at(curIndex + 1);
    }

    public void next() {
        if (nextIsIn(STOP_STR)) {
            // 当前指针后没有字符，直接返回空字符
            curIndex = source.length();
        } else {
            // 当前指针后仍有字符，移动并修改行列标识
            if (curIsIn(NEXTLINE)) {
                // 当前指针后为换行符，行标记自增，列标记还原
                curLine++;
                curRow = 1;
            } else {
                // 当前指针后不为换行符，列标记自增
                curRow++;
                if (curIsIn(TAB_STR)) {
                    curRow += TAB_SPACE - 1;
                }
            }
            at(++curIndex);
        }
    }

    public Pair<Integer, Integer> curPos() {
        return new Pair<>(curLine, curRow);
    }

    public void useCharSet(boolean use) {
        ignoreCharSet = !use;
    }

    /*----- 行列依赖工具函数 -----*/
    public boolean nextIsIn(String s) {
        return charIsIn(at(curIndex + 1), s);
    }

    public boolean curIsIn(String s) {
        return charIsIn(at(curIndex), s);
    }

    /*----- 无行列依赖工具函数 -----*/
    private char at(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Called SourceIterator.at(" + index + ")!");
        }

        if (index >= source.length()) {
            return STOP;
        }

        char c = source.charAt(index);
        if (charIsIn(c, charSet) || ignoreCharSet) {
            return c;
        }
        throw new IllegalArgumentException("Unsupported Character:'" + c + "'!");
    }

    private boolean charIsIn(char c, String s) {
        return s.indexOf(c) >= 0;
    }
}








