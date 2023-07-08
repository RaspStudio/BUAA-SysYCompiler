package frontend;

import frontend.label.meta.VarLabel;
import frontend.token.meta.KeyWordToken;
import frontend.token.meta.SymbolToken;
import frontend.tree.CompUnitNode;
import frontend.tree.LabelNode;
import frontend.tree.MetaNode;
import frontend.tree.exp.AddExpNode;
import frontend.tree.exp.CondNode;
import frontend.tree.exp.EqExpNode;
import frontend.tree.exp.ExpNode;
import frontend.tree.exp.LAndExpNode;
import frontend.tree.exp.LOrExpNode;
import frontend.tree.exp.LValNode;
import frontend.tree.exp.MulExpNode;
import frontend.tree.exp.PrimaryExpNode;
import frontend.tree.exp.RelExpNode;
import frontend.tree.exp.UnaryExpNode;
import frontend.tree.function.FuncCallNode;
import frontend.tree.function.FuncFParamNode;
import frontend.tree.function.FunctionNode;
import frontend.tree.stmt.AssignNode;
import frontend.tree.stmt.BlockNode;
import frontend.tree.stmt.BranchNode;
import frontend.tree.stmt.ExpStmtNode;
import frontend.tree.stmt.InputNode;
import frontend.tree.stmt.LoopCtrlNode;
import frontend.tree.stmt.LoopNode;
import frontend.tree.stmt.OutputNode;
import frontend.tree.stmt.ReturnNode;
import frontend.tree.stmt.StmtNode;
import frontend.tree.var.InitValNode;
import frontend.tree.var.VarDefNode;
import llvmir.tree.Module;
import llvmir.tree.SymbolTable;
import llvmir.tree.type.ArrayType;
import llvmir.tree.type.IntegerType;
import llvmir.tree.type.PointerType;
import llvmir.tree.type.Type;
import llvmir.tree.type.Types;
import llvmir.tree.type.VoidType;
import llvmir.tree.value.Argument;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.Constant;
import llvmir.tree.value.user.constant.data.ConstantDataArray;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.data.ConstantString;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.constant.global.GlobalVariable;
import llvmir.tree.value.user.instruction.AllocaInst;
import llvmir.tree.value.user.instruction.CallInst;
import llvmir.tree.value.user.instruction.GetElementPtrInst;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.LoadInst;
import llvmir.tree.value.user.instruction.NormalInstruction;
import llvmir.tree.value.user.instruction.StoreInst;
import llvmir.tree.value.user.instruction.ZExtInst;
import llvmir.tree.value.user.instruction.binary.AddInst;
import llvmir.tree.value.user.instruction.binary.ICmpInst;
import llvmir.tree.value.user.instruction.binary.MulInst;
import llvmir.tree.value.user.instruction.binary.SDivInst;
import llvmir.tree.value.user.instruction.binary.SRemInst;
import llvmir.tree.value.user.instruction.binary.SubInst;
import llvmir.tree.value.user.instruction.terminator.BrInst;
import llvmir.tree.value.user.instruction.terminator.RetInst;
import llvmir.tree.value.user.instruction.terminator.TerminateInstruction;
import util.Pair;
import util.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public abstract class IRBuilder {

    /*========== 中间代码树生成接口 ==========*/
    public static Module buildModule(CompUnitNode compUnit) {
        List<GlobalVariable> variables = new ArrayList<>();
        List<Function> functions = new ArrayList<>();
        Module ret = new Module();
        compUnit.getTopVariables().forEach(o -> variables.add(buildGlobalVar(o, ret.getTable())));
        compUnit.getTopFunctions().forEach(o -> functions.add(buildFunction(o, ret, ret.getTable())));
        return ret.register(variables, functions);
    }

    /*---------- 顶层元素：全局变量及函数 ----------*/
    private static GlobalVariable buildGlobalVar(VarDefNode varDef, SymbolTable topTable) {
        Type varType = getType(varDef);
        if (varType instanceof IntegerType || varType instanceof ArrayType) {
            // 整数类型全局变量
            GlobalVariable wrapped = new GlobalVariable(
                    varType, varDef.name(),
                    getConst(varDef.getValue()),
                    varDef.label().isConst()
            );
            topTable.add(varDef.name(), wrapped);
            return wrapped;
        } else {
            // 其它可拓展类型
            throw new UnsupportedOperationException("Unknown Variable Type!");
        }
    }

    private static Function buildFunction(FunctionNode func, Module parent, SymbolTable topTable) {
        // 建立函数上下文符号表
        SymbolTable derived = topTable.derive();
        Type retType = getType(func.label().returnLabel());
        Function ret = new Function(func.name(), parent, retType, derived);
        topTable.add(func.name(), ret);

        // 建立入口基本块
        BasicBlock topBlock = new BasicBlock(func.name() + ".entry", ret);
        // 建立返回值
        if (retType instanceof IntegerType) {
            Value var = buildLocalAlloca(func.name() + ".ret", retType, topBlock, derived);
            StoreInst store = new StoreInst(topBlock, new ConstantInt(0), var);
            topBlock.addInst(store);
            ret.setReturnValue(var);
        }

        // 建立出口基本块
        BasicBlock finalBlock = new BasicBlock(func.name() + ".exit", ret);

        // 匹配参数和基本块
        List<Argument> arguments = buildArguments(func, topBlock, derived);// 在此处已经向函数中加入了顶层基本块
        BasicBlock topAfter = buildBasicBlock(topBlock, finalBlock, func.getBlock(), null, null, derived);

        if (topAfter != null && topAfter.canRegister()) {
            if (retType instanceof VoidType) {
                if (topAfter != topBlock) {
                    topAfter.register(new BrInst(topAfter, finalBlock));
                } else {
                    topAfter.register(new BrInst(topAfter, finalBlock), false);
                }
            } else {
                throw new RuntimeException("No Return");
            }
        }

        // 完善函数属性并返回
        if (retType instanceof VoidType) {
            finalBlock.register(new RetInst(finalBlock));
        } else {
            LoadInst loadRet = new LoadInst(finalBlock, ret.getReturnValue());
            finalBlock.addInst(loadRet);
            finalBlock.register(new RetInst(finalBlock, loadRet));
        }
        return ret.register(arguments);
    }

    private static List<Argument> buildArguments(FunctionNode func, BasicBlock entrance, SymbolTable entranceTable) {
        // 匹配参数
        List<FuncFParamNode> funcFParams = func.getFakeParams();
        List<Argument> arguments = new ArrayList<>();
        List<Instruction> initialInsts = new ArrayList<>();
        int argNo = 0;
        for (FuncFParamNode f : funcFParams) {
            Type paramType = getType(f.label());
            Type realType;
            if (paramType instanceof IntegerType) {
                // int -> 分配指令返回 int*
                realType = paramType;
            } else if (paramType instanceof ArrayType) {
                // int[x]/int[x][y] -> 返回 int* / int**
                // 实际传入的类型
                realType = Types.pointer(((ArrayType) paramType).getDerivedType());
            } else if (paramType instanceof PointerType) {
                // int(*)[m] / int(*)
                realType = paramType;
            } else {
                throw new UnsupportedOperationException("Unknown Parameter Type!");
            }
            Argument arg = new Argument(realType, f.getName(), argNo++);
            arguments.add(arg);
            AllocaInst var = buildLocalAlloca(f.getName(), realType, entrance, entranceTable);
            StoreInst store = new StoreInst(entrance, arg, var);
            initialInsts.add(store);
            arg.setAlloca(var);
        }
        entrance.setEntrance(initialInsts);
        return arguments;
    }

    /*---------- 函数参数、基本块 ----------*/

    /**
     * 处理传入的基本块元素。
     * 基本块结束时就截止。
     */
    private static BasicBlock buildBasicBlock(BasicBlock start, BasicBlock exit,
                                              BlockNode node,
                                              BasicBlock breakTo, BasicBlock continueTo, SymbolTable curTable) {

        BasicBlock curBlock = start;

        for (MetaNode o : node.getContent()) {
            if (o instanceof VarDefNode) {
                buildVarDef((VarDefNode) o, curBlock, curTable);
            } else {
                BasicBlock ret =
                        buildStmt((StmtNode) o, curBlock, exit, breakTo, continueTo, curTable);
                if (ret != curBlock) {
                    // 更新当前活跃基本块信息
                    curBlock = ret;
                    // 若下层返回为空，则当前块节点后续指令已不可达，退出分析（并触发上层基本块的情况三）
                    if (ret == null) {
                        return null;
                    }
                }
            }
        }

        return curBlock;
    }

    /**
     * 处理传入的语句变量。
     * 若本函数中处理的语句使前面的基本块结算，则会提交终结语句，返回新的空基本块，由原函数进行提交。
     */
    private static BasicBlock buildStmt(StmtNode o, BasicBlock parent, BasicBlock exit,
                                        BasicBlock breakTo, BasicBlock continueTo, SymbolTable curTable) {
        if (o instanceof ExpStmtNode) {
            return buildExpStmt((ExpStmtNode) o, parent, curTable);
        } else if (o instanceof AssignNode) {
            return buildAssignStmt((AssignNode) o, parent, curTable);
        } else if (o instanceof InputNode) {
            return buildInputStmt((InputNode) o, parent, curTable);
        } else if (o instanceof OutputNode) {
            return buildOutputStmt((OutputNode) o, parent, curTable, parent.getParent().getParent());
        } else if (o instanceof ReturnNode) {
            return buildReturnStmt((ReturnNode) o, parent, exit, curTable);
        } else if (o instanceof BlockNode) {
            return buildBasicBlock(parent, exit, (BlockNode) o, breakTo, continueTo, curTable.derive());
        } else if (o instanceof BranchNode) {
            return buildBranchStmt((BranchNode) o, parent, exit, breakTo, continueTo, curTable);
        } else if (o instanceof LoopNode) {
            return buildLoopStmt((LoopNode) o, parent, exit, curTable);
        } else if (o instanceof LoopCtrlNode) {
            return buildLoopCtrlStmt((LoopCtrlNode) o, parent, breakTo, continueTo);
        } else {
            throw new UnsupportedOperationException("Unknown Stmt!");
        }
    }

    private static void buildVarDef(VarDefNode o, BasicBlock parent, SymbolTable currentTable) {
        Type varType = getType(o.label());
        if (varType instanceof IntegerType) {
            AllocaInst alloca = buildLocalAlloca(o.name(), varType, parent, currentTable);
            if (!o.isUndefined()) {
                // 常量声明
                buildAssign(
                        alloca,
                        o.label().isConst() ?
                                new ConstantInt(o.getValue().getValue(Collections.emptyList())) :
                                buildExp(o.getValue().getExp(), parent, currentTable),
                        parent
                );
            }
        } else if (varType instanceof ArrayType) {
            // 数组类型（ArrayType）
            AllocaInst alloca = buildLocalAlloca(o.name(), varType, parent, currentTable);
            if (!o.isUndefined()) {
                // 常量数组（常量初始化值）和已初始化数组
                // 初始化遍历器
                List<Integer> stop = ((ArrayType) varType).getDimensions();
                List<Integer> current = new ArrayList<>();
                for (int i = 0; i < stop.size(); i++) {
                    current.add(0);
                }

                do {
                    // 包裹寻址并赋值
                    List<Value> wrapped = new ArrayList<>();
                    for (int i : current) {
                        wrapped.add(new ConstantInt(i));
                    }
                    GetElementPtrInst ptr = GetElementPtrInst.build(parent, alloca, wrapped);
                    parent.addInst(ptr);
                    buildAssign(
                            ptr,
                            o.label().isConst() ?
                                    new ConstantInt(o.getValue().getValue(current)) :
                                    buildExp(o.getValue().getExp(current), parent, currentTable),
                            parent);
                    // 自增
                } while ((current = Tools.increase(current, stop)) != null);
            }
        } else {
            throw new UnsupportedOperationException("Unknown VarDef Type");
        }
    }

    /*---------- 分支和循环语句 ----------*/
    private static BasicBlock buildBranchStmt(BranchNode node, BasicBlock start, BasicBlock exit,
                                              BasicBlock breakTo, BasicBlock continueTo, SymbolTable curTable) {
        int id = Value.allocId(Value.Region.Branch);
        BasicBlock trueBlock = new BasicBlock("if" + id + ".true", start.getParent());
        BasicBlock falseBlock = node.getElseStmt() == null ? null
                : new BasicBlock("if" + id + ".false", start.getParent());
        BasicBlock afterBlock = new BasicBlock("if" + id + ".end", start.getParent());



        if (falseBlock == null) {
            // 单分支
            buildCond(node.getCond(), "if" + id + ".cond", start, trueBlock, afterBlock, curTable);
        } else {
            // 双分支
            buildCond(node.getCond(),"if" + id + ".cond", start, trueBlock, falseBlock, curTable);
        }

        BasicBlock afterTrue = buildBasicBlock(trueBlock, exit, node.getIfStmt(),
                breakTo, continueTo, curTable.derive());

        if (afterTrue != null) {
            afterTrue.register(new BrInst(afterTrue, afterBlock));
        }

        if (falseBlock != null) {
            BasicBlock afterFalse = buildBasicBlock(falseBlock, exit, node.getElseStmt(),
                    breakTo, continueTo, curTable.derive());

            if (afterFalse != null) {
                afterFalse.register(new BrInst(afterFalse, afterBlock));
            }
        }


        return afterBlock;
    }

    private static BasicBlock buildLoopStmt(LoopNode node, BasicBlock start, BasicBlock exit, SymbolTable curTable) {
        int id = Value.allocId(Value.Region.Loop);
        BasicBlock condBlock = new BasicBlock("loop" + id + ".condentry", start.getParent());
        BasicBlock loopBlock = new BasicBlock("loop" + id + ".body", start.getParent());
        BasicBlock afterLoop = new BasicBlock("loop" + id + ".end", start.getParent());

        start.register(new BrInst(start, condBlock));
        buildCond(node.getCond(), "loop" + id + ".cond", condBlock, loopBlock, afterLoop, curTable);

        BasicBlock loopBlockAfter = buildBasicBlock(loopBlock, exit, node.getStmt(),
                afterLoop, condBlock, curTable.derive());
        if (loopBlockAfter != null) {
            loopBlockAfter.register(new BrInst(loopBlockAfter, condBlock));
        }
        return afterLoop;
    }

    /*---------- 逻辑表达式分支语句 ----------*/
    private static void buildCond(CondNode o, String name, BasicBlock start,
                                        BasicBlock trueTo, BasicBlock falseTo, SymbolTable curTable) {
        buildLOrExp(o.getOrExp(), name, start, trueTo, falseTo, curTable);
    }

    private static void buildLOrExp(LOrExpNode node, String name, BasicBlock start,
                                          BasicBlock trueTo, BasicBlock falseTo, SymbolTable curTable) {
        List<LAndExpNode> andExpNodes = node.getAndExps();
        List<BasicBlock> flows = buildFlow(name, Value.Region.LOr, andExpNodes.size(), start, falseTo);

        for (int i = 0; i < andExpNodes.size(); i++) {
            BasicBlock curBlock = flows.get(i);
            BasicBlock curFalseBlock = flows.get(i + 1);
            buildLAndExp(andExpNodes.get(i), name,
                    curBlock, trueTo, curFalseBlock, curTable);
        }
    }

    private static void buildLAndExp(LAndExpNode node, String name, BasicBlock start,
                                    BasicBlock trueTo, BasicBlock falseTo, SymbolTable curTable) {
        List<EqExpNode> eqExpNodes = node.getEqExps();
        List<BasicBlock> flows = buildFlow(name, Value.Region.LAnd, eqExpNodes.size(), start, trueTo);

        for (int i = 0; i < eqExpNodes.size(); i++) {
            BasicBlock curBlock = flows.get(i);
            BasicBlock curTrueBlock = flows.get(i + 1);
            buildEqExp(eqExpNodes.get(i),
                    curBlock, curTrueBlock, falseTo, curTable);
        }

    }

    private static void buildEqExp(EqExpNode node, BasicBlock start,
                                   BasicBlock trueTo, BasicBlock falseTo, SymbolTable curTable) {
        // 先计算表达式，若有提交通道，提交不注册；否则不提交注册新块
        List<RelExpNode> relExpNodes = node.getRelExps();
        List<SymbolToken> ops = node.getOps();

        Value curValue = buildRelExp(relExpNodes.get(0), start, curTable);

        for (int i = 0; i < ops.size(); i++) {
            Value nextValue = buildRelExp(relExpNodes.get(i + 1), start, curTable);
            if (curValue.getType() == Types.BOOL) {
                curValue = new ZExtInst(start, curValue, Types.INT);
                start.addInst((NormalInstruction) curValue);
            }
            ICmpInst.CmpType cmpType;
            switch (ops.get(i).getContent()) {
                case "==":
                    cmpType = ICmpInst.CmpType.EQ;
                    break;
                case "!=":
                    cmpType = ICmpInst.CmpType.NE;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown Op");
            }
            start.addInst(
                    (NormalInstruction) (curValue = new ICmpInst("iseq",
                            start, cmpType, curValue, nextValue))
            );
        }

        if (curValue.getType() == Types.INT) {
            curValue = new ICmpInst("bool", start, ICmpInst.CmpType.NE,
                    curValue, new ConstantInt(0));
            start.addInst((NormalInstruction) curValue);
        }

        // 获得了布尔值计算结果，提交
        TerminateInstruction branch = new BrInst(start, curValue, trueTo, falseTo);
        start.register(branch);
    }

    private static Value buildRelExp(RelExpNode node, BasicBlock start, SymbolTable curTable) {
        List<AddExpNode> addExpNodes = node.getAddExps();
        List<SymbolToken> ops = node.getOps();

        Value curValue = buildAddExp(addExpNodes.get(0), start, curTable);

        for (int i = 0; i < ops.size(); i++) {
            Value nextValue = buildAddExp(addExpNodes.get(i + 1), start, curTable);
            ICmpInst.CmpType cmpType;
            switch (ops.get(i).getContent()) {
                case ">":
                    cmpType = ICmpInst.CmpType.SGT;
                    break;
                case ">=":
                    cmpType = ICmpInst.CmpType.SGE;
                    break;
                case "<":
                    cmpType = ICmpInst.CmpType.SLT;
                    break;
                case "<=":
                    cmpType = ICmpInst.CmpType.SLE;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown Op");
            }
            ICmpInst cmpInst = new ICmpInst("is" + cmpType.name().toLowerCase(),
                    start, cmpType, curValue, nextValue);
            start.addInst(cmpInst);
            curValue = new ZExtInst(start, cmpInst, Types.INT);
            start.addInst((NormalInstruction) curValue);
        }

        return curValue;
    }

    /*---------- 逻辑表达式分支结构辅助工具 ----------*/
    private static List<BasicBlock> buildFlow(String name, Value.Region region,
                                              int expNum, BasicBlock start, BasicBlock end) {
        List<BasicBlock> flow = new ArrayList<>();
        flow.add(start);
        for (int i = 1; i < expNum; i++) {
            flow.add(new BasicBlock(name + region.name().toLowerCase() + i, start.getParent()));
        }
        flow.add(end);
        return flow;
    }

    /*---------- 基本块终结语句 ----------*/
    private static BasicBlock buildReturnStmt(ReturnNode node, BasicBlock parent, BasicBlock exit, SymbolTable table) {
        Pair<Boolean, ExpNode> content = node.getContent();
        if (!content.getLeft()) {
            // 整形返回值
            parent.addInst(new StoreInst(parent, buildExp(content.getRight(), parent, table),
                    parent.getParent().getReturnValue()));
            parent.register(new BrInst(parent, exit));
        } else {
            // 无返回值
            parent.register(new BrInst(parent, exit));
        }
        return null;
    }

    private static BasicBlock buildLoopCtrlStmt(LoopCtrlNode node, BasicBlock parent,
                                                BasicBlock breakTo, BasicBlock continueTo) {
        if (node.getCtrl().equals(KeyWordToken.CONTINUETK)) {
            parent.register(new BrInst(parent, continueTo));
        } else if (node.getCtrl().equals(KeyWordToken.BREAKTK)) {
            parent.register(new BrInst(parent, breakTo));
        } else {
            throw new RuntimeException("Unknown Loop Ctrl");
        }
        return null;
    }

    /*---------- 普通操作语句 ----------*/
    private static BasicBlock buildExpStmt(ExpStmtNode o, BasicBlock parent, SymbolTable curTable) {
        if (o.getExp() != null) {
            buildExp(o.getExp(), parent, curTable);
        }
        return parent;
    }

    private static BasicBlock buildOutputStmt(OutputNode node, BasicBlock parent,
                                              SymbolTable currentTable, Module module) {
        List<Value> params = new ArrayList<>();
        node.getExps().forEach(o -> params.add(buildExp(o, parent, currentTable)));
        List<String> strings = Arrays.asList(
                node.getFormatString().getContent().replaceAll("\"", "").split("%d", -1)
        );

        Function putint = (Function) currentTable.get(Function.OUTPUT_INT_FUNC);
        Function putstr = (Function) currentTable.get(Function.OUTPUT_STR_FUNC);

        for (int i = 0; i < strings.size(); i++) {
            if (i > 0) {
                parent.addInst(CallInst.createCall(putint, parent, Collections.singletonList(params.get(i - 1))));
            }
            if (strings.get(i).length() > 0) {
                GlobalVariable strVar = module.addStringConst(new ConstantString(strings.get(i)));
                GetElementPtrInst strPtr = new GetElementPtrInst(strVar, parent);
                parent.addInst(strPtr);
                parent.addInst(CallInst.createCall(putstr, parent, Collections.singletonList(strPtr)));
            }
        }
        return parent;
    }

    private static BasicBlock buildInputStmt(InputNode o, BasicBlock parent, SymbolTable currentTable) {
        CallInst call = CallInst.createCall(
                (Function) currentTable.get(Function.INPUT_FUNC), parent, Collections.emptyList());
        parent.addInst(call);
        buildAssign(buildLVal(o.getDest(), parent, currentTable), call, parent);
        return parent;
    }

    private static BasicBlock buildAssignStmt(AssignNode o, BasicBlock parent, SymbolTable currentTable) {
        buildAssign(
                buildLVal(o.getDest(), parent, currentTable),
                buildExp(o.getExp(), parent, currentTable),
                parent
        );
        return parent;
    }

    /*---------- 赋值辅助函数 ----------*/
    private static void buildAssign(Value dest, Value val,  BasicBlock parent) {
        parent.addInst(new StoreInst(parent, val, dest));
    }

    /*---------- 局部变量声明及引用 ----------*/
    private static Value buildLVal(LValNode node, BasicBlock parent, SymbolTable currentTable) {
        Type lvalType = getType(node.topLabel());
        if (lvalType instanceof IntegerType) {
            // 单整形变量（返回引用）
            return currentTable.get(node.name());
        } else if (lvalType instanceof ArrayType || lvalType instanceof PointerType) {
            // 数组变量（返回引用）
            Value dest = currentTable.get(node.name());
            List<Value> indices = new ArrayList<>();
            node.getDimensions().forEach(o -> indices.add(buildExp(o, parent, currentTable)));
            if (indices.size() > 0) {
                GetElementPtrInst ptr = GetElementPtrInst.build(parent, dest, indices);
                parent.addInst(ptr);
                return ptr;
            } else {
                return dest;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported LVal Type");
        }
    }

    /**
     * 分配本地变量空间：返回 type*
     */
    private static AllocaInst buildLocalAlloca(String varName, Type type,
                                               BasicBlock parent, SymbolTable table) {
        if (type instanceof IntegerType || type instanceof ArrayType || type instanceof PointerType) {
            // 局部整数、局部和参数数组、参数中的指针
            AllocaInst alloca = new AllocaInst(type, varName, parent);
            parent.addInst(alloca);
            table.add(varName, alloca);
            return alloca;
        } else {
            // 可拓展类型
            throw new UnsupportedOperationException("Unsupported Local Var Type!");
        }

    }

    /*---------- 算数表达式建立 ----------*/
    private static Value buildExp(ExpNode exp, BasicBlock parent, SymbolTable table) {
        return buildAddExp(exp.getAddExp(), parent, table);
    }

    private static Value buildAddExp(AddExpNode addExpNode, BasicBlock parent, SymbolTable table) {
        Pair<List<MulExpNode>, List<SymbolToken>> node = addExpNode.getMulExps();
        List<MulExpNode> exps = node.getLeft();
        List<SymbolToken> ops = node.getRight();

        Value cur = buildMulExp(exps.get(0), parent, table);
        for (int i = 0; i < ops.size(); i++) {
            Value next = buildMulExp(exps.get(i + 1), parent, table);
            NormalInstruction newInst;
            switch (ops.get(i).getContent()) {
                case "+":
                    newInst = new AddInst(Types.INT, ops.get(i).getVarName(), parent, cur, next);
                    break;
                case "-":
                    newInst = new SubInst(Types.INT, ops.get(i).getVarName(), parent, cur, next);
                    break;
                default:
                    throw new RuntimeException("Unknown Op!");
            }
            parent.addInst(newInst);
            cur = newInst;
        }

        return cur;
    }

    private static Value buildMulExp(MulExpNode mulExpNode, BasicBlock parent, SymbolTable table) {
        Pair<List<UnaryExpNode>, List<SymbolToken>> node = mulExpNode.getMulExps();
        List<UnaryExpNode> exps = node.getLeft();
        List<SymbolToken> ops = node.getRight();

        Value cur = buildUnaryExp(exps.get(0), parent, table);
        for (int i = 0; i < ops.size(); i++) {
            Value next = buildUnaryExp(exps.get(i + 1), parent, table);
            NormalInstruction newInst;
            switch (ops.get(i).getContent()) {
                case "*":
                    newInst = new MulInst(Types.INT, ops.get(i).getVarName(), parent, cur, next);
                    break;
                case "/":
                    newInst = new SDivInst(Types.INT, ops.get(i).getVarName(), parent, cur, next);
                    break;
                case "%":
                    newInst = new SRemInst(Types.INT, ops.get(i).getVarName(), parent, cur, next);
                    break;
                default:
                    throw new RuntimeException("Unknown Op!");
            }
            parent.addInst(newInst);
            cur = newInst;
        }

        return cur;
    }

    private static Value buildUnaryExp(UnaryExpNode unaryExpNode, BasicBlock parent, SymbolTable table) {
        Pair<Pair<String, Stack<Boolean>>, Pair<FuncCallNode, PrimaryExpNode>>
                content = unaryExpNode.getContent();
        Stack<Boolean> isNotElseNeg = content.getLeft().getRight();

        Value unaryExp;
        if (content.getRight().getLeft() != null) {
            // 函数调用
            unaryExp = buildFuncCall(content.getRight().getLeft(), parent, table);
        } else {
            // 基础表达式
            unaryExp = buildPrimaryExp(content.getRight().getRight(), parent, table);
        }

        while (!isNotElseNeg.isEmpty()) {
            if (isNotElseNeg.pop()) {
                // 逻辑非
                unaryExp = new ICmpInst("not." + content.getLeft().getLeft(), parent,
                        ICmpInst.CmpType.EQ, ConstantInt.ZERO, unaryExp);
                parent.addInst((NormalInstruction) unaryExp);
                unaryExp = new ZExtInst(parent, unaryExp, Types.INT);
                parent.addInst((NormalInstruction) unaryExp);
            } else {
                // 负号
                unaryExp = new SubInst(Types.INT,"neg." + content.getLeft().getLeft(), parent,
                        ConstantInt.ZERO, unaryExp);
                parent.addInst((NormalInstruction) unaryExp);
            }
        }

        return unaryExp;
    }

    private static Value buildFuncCall(FuncCallNode node, BasicBlock parent, SymbolTable table) {
        List<ExpNode> expNodes = node.getRparams();
        List<Value> args = new ArrayList<>();
        expNodes.forEach(o -> args.add(buildExp(o, parent, table)));
        CallInst call = CallInst.createCall((Function) table.get(node.name()), parent, args);
        parent.addInst(call);
        return call;
    }

    private static Value buildPrimaryExp(PrimaryExpNode node, BasicBlock parent, SymbolTable table) {
        Pair<Integer, Pair<LValNode, ExpNode>> content = node.getContent();

        if (content.getLeft() != null) {
            return new ConstantInt(content.getLeft());
        } else if (content.getRight().getLeft() != null) {
            Value lval = buildLVal(content.getRight().getLeft(), parent, table);
            if (lval.getType() instanceof PointerType) {
                if (((PointerType) lval.getType()).getDerivedType() instanceof IntegerType) {
                    // 指针指向整数
                    LoadInst load = new LoadInst(parent, lval);
                    parent.addInst(load);
                    return load;
                } else {
                    // 指针指向数组todo
                    GetElementPtrInst ptr = GetElementPtrInst.build(parent, lval,
                            Collections.singletonList(ConstantInt.ZERO));
                    parent.addInst(ptr);
                    return ptr;
                }
            } else {
                return lval;
            }
        } else {
            return buildExp(content.getRight().getRight(), parent, table);
        }
    }

    /*========== 工具函数 : 获取变量类型 ==========*/
    private static Type getType(LabelNode<VarLabel> node) {
        return getType(node.label());
    }

    private static Type getType(VarLabel label) {
        Pair<KeyWordToken, List<Integer>> content = label.getType();
        if (content.getLeft().equals(KeyWordToken.INTTK)) {
            if (content.getRight().size() == 0) {
                return Types.INT;
            } else if (content.getRight().get(0) == null) {
                return Types.pointer(getType(label.subLabel()));
            } else {
                return Types.array(getType(label.subLabel()), content.getRight().get(0));
            }
        } else {
            return Types.VOID;
        }
    }

    /*========== 工具函数 : 获取常量 ==========*/
    private static Constant getConst(InitValNode val) {
        if (val.isConst()) {
            return new ConstantInt(val.getValue());
        } else if (val.isArray()) {
            List<Constant> array = new ArrayList<>();
            val.forEach(o -> array.add(getConst(o)));
            Type arrayType = Types.array(array.get(0).getType(), array.size());
            return new ConstantDataArray(arrayType, "const_arr" + Value.allocId(Value.Region.ConstArray), array);
        } else {
            throw new RuntimeException("Unknown Init Value");
        }
    }
}
