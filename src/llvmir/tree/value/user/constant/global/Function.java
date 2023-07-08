package llvmir.tree.value.user.constant.global;

import llvmir.tree.Module;
import llvmir.tree.SymbolTable;
import llvmir.tree.SymbolTabler;
import llvmir.tree.type.Type;
import llvmir.tree.type.Types;
import llvmir.tree.value.Argument;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.instruction.Instruction;

import java.util.*;

public class Function extends GlobalObject implements SymbolTabler {
    private final boolean isBuiltIn;
    private final Module parent;
    private final Type retType;
    private List<Argument> arguments;
    private List<BasicBlock> blocks;
    private final SymbolTable curTable;
    private Value returnValue = null;

    public Function(String name, Module parent, Type retType, SymbolTable curTable) {
        super(Types.function(), name, Collections.emptyList());
        this.isBuiltIn = false;
        this.parent = parent;
        this.retType = retType;
        this.arguments = null;
        this.blocks = null;
        this.curTable = curTable;
    }

    public Function(String name, Module parent, Type retType, SymbolTable topTable, List<Argument> arguments) {
        super(Types.function(), name, Collections.emptyList());
        this.isBuiltIn = true;
        this.parent = parent;
        this.retType = retType;
        this.arguments = arguments;
        this.blocks = new ArrayList<>();
        this.curTable = topTable;
    }

    public void setEntrance(BasicBlock block) {
        this.blocks = new ArrayList<>();
        this.blocks.add(block);
    }

    public Function register(List<Argument> arguments) {
        if (this.arguments == null) {
            this.arguments = arguments;
            return this;
        } else {
            throw new RuntimeException("Cannot Register");
        }
    }

    public void addBlock(BasicBlock block) {
        if (this.blocks.contains(block)) {
            if (block != this.blocks.get(0)) {
                throw new RuntimeException("Duplicated Block");
            }
        } else {
            this.blocks.add(block);
        }
    }

    public Type getRetType() {
        return retType;
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    public List<Argument> getArguments() {
        return new ArrayList<>(arguments);
    }

    public List<BasicBlock> getBlocks() {
        return new ArrayList<>(blocks);
    }

    public void trimBlocks() {
        boolean changed = true;
        while (changed) {
            changed = false;
            ListIterator<BasicBlock> iter = blocks.listIterator();
            while (iter.hasNext()) {
                BasicBlock block = iter.next();
                if (block.getFroms().isEmpty() && !block.isEntry()) {
                    disableBlock(block);
                    iter.remove();
                    changed = true;
                }
            }
        }
    }

    private void disableBlock(BasicBlock block) {
        if (!blocks.contains(block) || block.isEntry()) {
            throw new RuntimeException("Cannot Remove");
        }
        // 移除方程中所有指令
        for (Instruction inst : block.getAllInstructions()) {
            block.removeInst(inst);
        }
        // 移除该块后继块的引用
        for (BasicBlock to : block.getTos()) {
            to.removeFrom(block);
        }
        // 移除该块前驱块的引用
        if (block.getFroms().size() != 0) {
            throw new RuntimeException("Who Called Remove?");
        }
    }

    public void setReturnValue(Value returnValue) {
        this.returnValue = returnValue;
    }

    public Value getReturnValue() {
        if (returnValue == null) {
            throw new RuntimeException("Haven't Register!");
        }
        return returnValue;
    }

    @Override
    public Module getParent() {
        return parent;
    }

    @Override
    public SymbolTable getTable() {
        return curTable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(/*";========== Function ==========\n"*/);
        sb.append(isBuiltIn ? "declare" : "define");
        sb.append(" dso_local ").append(retType).append(" ").append(name);
        sb.append("(");
        StringJoiner argumentsJoiner = new StringJoiner(", ");
        arguments.forEach(o -> argumentsJoiner.add(isBuiltIn ? o.getType().toString() : o.toString()));
        sb.append(argumentsJoiner).append(") ");

        if (!isBuiltIn) {
            sb.append("{\n");
            StringJoiner blocksJoiner = new StringJoiner("\n");
            blocks.forEach(o -> blocksJoiner.add(o.toString()));
            sb.append(blocksJoiner).append("\n}\n");
        }

        return sb.toString();
    }

    public static final String INPUT_FUNC = "getint";
    public static final String OUTPUT_INT_FUNC = "putint";
    public static final String OUTPUT_STR_FUNC = "putstr";

}
