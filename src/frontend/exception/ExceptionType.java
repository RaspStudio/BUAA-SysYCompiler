package frontend.exception;

public enum ExceptionType {
    InvalidString("a"),
    DuplicatedIdent("b"),
    UnknownIdent("c"),
    InvalidRParamNumber("d"),
    InvalidRParamType("e"),
    InvalidFuncReturn("f"),
    FuncNeedReturn("g"),
    ConstModification("h"),

    MissingSemiColon("i"),
    MissingRParent("j"),
    MissingRBrack("k"),

    InvalidOutputArgument("l"),
    InvalidLoopCtrl("m"),
    ;

    private final String code;

    ExceptionType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
