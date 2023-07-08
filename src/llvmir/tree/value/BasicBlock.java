package llvmir.tree.value;

import llvmir.tree.Derivative;
import llvmir.tree.SymbolTable;
import llvmir.tree.SymbolTabler;
import llvmir.tree.type.Types;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.NormalInstruction;
import llvmir.tree.value.user.instruction.StoreInst;
import llvmir.tree.value.user.instruction.terminator.TerminateInstruction;

import java.util.*;

public class BasicBlock extends Value implements Derivative<Function>, SymbolTabler {
    private final Function parent;
    private final List<Instruction> instructions;
    private TerminateInstruction terminator;
    private final List<BasicBlock> froms;
    private final List<BasicBlock> tos;
    private boolean isEntry = false;
    private int depth = 0;

    public BasicBlock(String name, Function parent) {
        super(Types.label(), "%" + name);
        this.parent = parent;
        this.instructions = new LinkedList<>();
        this.terminator = null;
        this.froms = new ArrayList<>();
        this.tos = new ArrayList<>();
    }

    /*---------- 前端 ----------*/
    public void setEntrance(List<Instruction> initArgumentInsts) {
        this.instructions.addAll(initArgumentInsts);
        this.parent.setEntrance(this);
        this.isEntry = true;
    }

    public void addInst(NormalInstruction instruction) {
        this.instructions.add(instruction);
    }

    public void register(TerminateInstruction terminator, boolean submit) {
        if (this.terminator == null) {
            this.terminator = terminator;
            if (submit) {
                this.parent.addBlock(this);
            }
        } else {
            throw new RuntimeException("Cannot Register Twice");
        }
    }

    public void register(TerminateInstruction terminator) {
        register(terminator, true);
    }

    public boolean canRegister() {
        return this.terminator == null;
    }

    public void addFrom(BasicBlock... block) {
        froms.addAll(Arrays.asList(block));
    }

    public void addTo(BasicBlock... block) {
        tos.addAll(Arrays.asList(block));
    }


    public void registerLoop(BasicBlock exit, BasicBlock loopBody) {
        this.depth++;
        loopBody.travelLoop(exit, this, new HashSet<>());
    }

    private void travelLoop(BasicBlock exit, BasicBlock cond, Set<BasicBlock> visited) {
        if (this == exit || this == cond) {
            return;
        }
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        this.depth++;
        for (BasicBlock to : tos) {
            to.travelLoop(exit, cond, visited);
        }
    }

    /*---------- 后端 ----------*/
    public int loopDepth() {
        return depth;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<Instruction> getAllInstructions() {
        List<Instruction> ret = new ArrayList<>(instructions);
        ret.add(terminator);
        return ret;
    }

    public TerminateInstruction getTerminator() {
        return terminator;
    }

    public List<BasicBlock> getFroms() {
        return new ArrayList<>(froms);
    }

    public List<BasicBlock> getTos() {
        return new ArrayList<>(tos);
    }

    public boolean isEntry() {
        return isEntry;
    }

    @Override
    public Function getParent() {
        return parent;
    }

    @Override
    public SymbolTable getTable() {
        return parent.getTable();
    }

    public void removeInst(Instruction value) {
        if (!instructions.remove(value) && terminator != value) {
            throw new RuntimeException("Cannot Remove Instruction");
        }
        new HashSet<>(value.getOperands()).forEach(o -> o.delUser(value));
    }

    public void moveInst(Instruction inst, Instruction before) {
        if (!instructions.remove(inst) || !(inst instanceof NormalInstruction)) {
            throw new RuntimeException("Cannot Remove Instruction");
        }
        before.getParent().insertBefore(before, inst);
        inst.setParent(before.getParent());
    }

    public void removeFrom(BasicBlock block) {
        if (!froms.remove(block)) {
            throw new RuntimeException("Cannot Remove From");
        }
    }

    public void insertHead(Instruction phi) {
        instructions.add(0, phi);
    }

    public void insertBefore(Instruction before, Instruction inst) {
        int index = instructions.indexOf(before);
        if (index >= 0) {
            instructions.add(index, inst);
        } else if (terminator == before) {
            instructions.add(inst);
        } else {
            throw new RuntimeException("Cannot Find Instruction");
        }
    }

    public void insertAfter(Instruction target, Instruction newInst) {
        int index = instructions.indexOf(target);
        if (index == -1) {
            throw new RuntimeException("Cannot Find Target Instruction");
        }
        instructions.add(index + 1, newInst);
    }

    public void insertTail(StoreInst storeInst) {
        instructions.add(storeInst);
    }

    public int indexOf(Instruction instruction) {
        if (terminator == instruction) {
            return instructions.size();
        }
        return instructions.indexOf(instruction);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        //joiner.add(String.format(";========== Block \"%s\" ==========", name.replaceAll("@","")));
        joiner.add(name.replaceAll("%","") + ":");
        instructions.forEach(o -> joiner.add(o.toString()));
        joiner.add(terminator.toString());
        return joiner.toString();
    }

}
