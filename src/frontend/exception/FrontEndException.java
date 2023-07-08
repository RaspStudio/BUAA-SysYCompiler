package frontend.exception;

public abstract class FrontEndException extends Exception implements Comparable<FrontEndException> {
    protected int line;
    protected int row;
    protected ExceptionType type;

    public FrontEndException(String s) {
        super(s);
        this.line = -1;
        this.row = -1;
        this.type = null;
    }

    public void set(ExceptionType type, int line, int row) {
        if (this.type == null) {
            this.type = type;
            this.line = line;
            this.row = row;
        } else {
            throw new UnsupportedOperationException("Cannot Set Type Twice!");
        }
    }

    @Override
    public int compareTo(FrontEndException o) {
        if (this.type == null) {
            throw new UnsupportedOperationException("Cannot Compare Object Before Set");
        }
        if (line == o.line) {
            if (type.code().equals(o.type.code())) {
                return row - o.row;
            }
            return type.code().compareTo(o.type.code());
        }
        return line - o.line;
    }

    @Override
    public String toString() {
        return type + " at (" + line + "," + row + ")";
    }

    public final String getExceptionCode() {
        return String.format("%d %s", line, type.code());
    }
}
