package frontend.token.meta;

public enum TokenType {
    // 宏观类
    Ident, Integer, String,
    // 特殊符号
    AddOp, MulOp, UnaryOp, RelOp, EqOp, BType,
    FuncType, LoopCtrl
}
