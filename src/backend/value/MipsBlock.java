package backend.value;

import backend.regalloc.VBlock;
import backend.translate.MipsMapper;
import backend.value.inst.MipsInst;
import backend.value.inst.MipsInstFactory;
import backend.value.inst.atype.MipsSaveWord;
import backend.value.inst.rtype.MipsAdd;
import backend.value.inst.special.MipsComment;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsLabel;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;
import llvmir.tree.value.BasicBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class MipsBlock extends MipsLabel implements VBlock<MipsInst, MipsReg, MipsBlock> {
    private final MipsFunction parent;
    private final List<MipsBlock> preds = new ArrayList<>();
    private final List<MipsBlock> succs = new ArrayList<>();
    private final List<MipsInst> instructions = new ArrayList<>();

    protected MipsBlock(String name, MipsFunction parent) {
        super(name);
        this.parent = parent;
    }

    @Override
    public List<MipsInst> getInsts() {
        return instructions;
    }

    @Override
    public List<MipsBlock> getPreds() {
        return preds;
    }

    @Override
    public List<MipsBlock> getSuccs() {
        return succs;
    }

    public MipsFunction getParent() {
        return parent;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n\t", name + ":\n\t", "\n");
        instructions.forEach(inst -> joiner.add(inst.toString()));
        return joiner.toString();
    }

    public static MipsBlock create(BasicBlock block, MipsMapper mapper) {
        MipsBlock mipsBlock = mapper.getBlock(block);

        // 每个基本块只能实例化一次
        if (!mipsBlock.instructions.isEmpty()) {
            throw new RuntimeException("Block " + block + " has already been translated");
        }

        // 如果是入口块，分配栈帧，存储返回地址
        if (block.isEntry()) {
            mipsBlock.instructions.add(new MipsAdd(mipsBlock, MipsReg.of(MipsRegs.SP), MipsReg.of(MipsRegs.SP),
                    MipsImm.of(() -> -mipsBlock.getParent().stack().curStackSize().get())));
            mipsBlock.instructions.add(new MipsSaveWord(mipsBlock, MipsReg.of(MipsRegs.RA),
                    mipsBlock.getParent().stack().getCurFuncRa()));
        }

        // 实例化各个指令 todo 预分配的寄存器的对象的生命周期混乱
        block.getAllInstructions().forEach(o -> {
            mipsBlock.instructions.addAll(MipsInstFactory.translate(o, mipsBlock, mapper));
            mipsBlock.instructions.add(new MipsComment(mipsBlock, o.toString()));
        });
        // 初始化各个前驱块
        block.getFroms().forEach(o -> mipsBlock.preds.add(mapper.getBlock(o)));
        // 初始化各个后继块
        block.getTos().forEach(o -> mipsBlock.succs.add(mapper.getBlock(o)));

        return mipsBlock;
    }
}
