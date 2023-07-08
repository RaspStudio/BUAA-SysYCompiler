package backend.value;

import backend.regalloc.VFunc;
import backend.translate.MipsMapper;
import backend.translate.MipsStack;
import backend.value.inst.MipsInst;
import backend.value.meta.MipsLabel;
import backend.value.meta.MipsReg;
import llvmir.tree.value.user.constant.global.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class MipsFunction extends MipsLabel implements VFunc<MipsInst, MipsReg, MipsBlock> {
    private final MipsStack stack = new MipsStack();
    private final List<MipsBlock> blocks = new ArrayList<>();

    public MipsFunction(String name) {
        super(name);
    }

    public MipsStack stack() {
        return stack;
    }

    @Override
    public List<MipsBlock> getBlocks() {
        return blocks;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n\n", name + ":\n", "");
        joiner.add("### " + stack);
        blocks.forEach(o -> joiner.add(o.toString()));
        return joiner.toString();
    }

    public static MipsFunction create(Function function, MipsMapper mapper) {
        MipsFunction mipsFunction = mapper.getFunction(function);
        // 每个函数只能实例化一次
        if (!mipsFunction.blocks.isEmpty()) {
            throw new RuntimeException("Function " + function + " has already been translated");
        }
        // 初始化各个初始块，便于跳转标签的查找
        function.getBlocks().forEach(o -> mapper.putLabel(o, new MipsBlock(o.getPureName(), mipsFunction)));
        // 初始化各个函数参数，便于参数的查找（todo：暂时认为参数全存在内存里）
        function.getArguments().forEach(o -> mapper.putArgPointer(o, mipsFunction.stack.getCurFuncArg(o.getArgNo())));
        // 实例化各个基本块
        function.getBlocks().forEach(o -> mipsFunction.blocks.add(MipsBlock.create(o, mapper)));

        return mipsFunction;
    }
}
