package frontend.syntax.factories;

public enum SyntaxType {
    Wrapper(),
    CompUnit("CompUnit"),

    // 变量类
    ConstDecl("ConstDecl"),
    VarDecl("VarDecl"),
    ConstDef("ConstDef"),
    VarDef("VarDef"),
    ConstInitVal("ConstInitVal"),
    InitVal("InitVal"),

    // 函数类
    FuncDef("FuncDef"),
    MainFuncDef("MainFuncDef"),
    FuncType("FuncType"),
    FuncFParams("FuncFParams"),
    FuncFParam("FuncFParam"),
    FuncRParams("FuncRParams"),
    FuncCall(),


    // 语句类
    Block("Block"),
    BlockItem(),
    IfElseBranch("Stmt"),
    WhileLoop("Stmt"),
    LoopCtrl("Stmt"),
    Return("Stmt"),
    Input("Stmt"),
    Output("Stmt"),
    Assign("Stmt"),
    ExpStmt("Stmt"),
    BlockStmt("Stmt"),

    // 表达式类
    // 逻辑表达式
    Cond("Cond"),
    LOrExp("LOrExp"),
    LAndExp("LAndExp"),
    EqExp("EqExp"),
    RelExp("RelExp"),

    // 顶层表达式
    Exp("Exp"),
    LVal("LVal"),
    ConstExp("ConstExp"),

    // 算数表达式
    AddExp("AddExp"),
    MulExp("MulExp"),
    UnaryExp("UnaryExp"),
    PrimaryExp("PrimaryExp"),

    // 其它表达式
    UnaryOp("UnaryOp"),
    Number("Number")
    ;

    private final String code;
    SyntaxType(String code) {
        this.code = code;
    }

    SyntaxType() {
        this.code = null;
    }

    public String getTokenCode() {
        return code;
    }
}
