package backend;

import backend.regalloc.Allocator;
import backend.translate.MipsMapper;
import backend.value.MipsFunction;
import backend.value.data.MipsData;
import backend.value.meta.MipsRegs;
import llvmir.tree.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class BackEnd {
    private final List<MipsData> datas = new ArrayList<>();
    private final List<MipsFunction> functions = new ArrayList<>();
    private final MipsMapper mapper = new MipsMapper();

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(".data");
        datas.forEach(o -> joiner.add(o.toString()));
        joiner.add(".text");
        joiner.add("\tjal\t\tmain").add("\tli\t\t$v0, 10").add("\tsyscall");
        functions.forEach(f -> joiner.add(f.toString()));
        return joiner.toString();
    }

    public static BackEnd build(Module module) {
        BackEnd backEnd = new BackEnd();
        MipsMapper mapper = backEnd.mapper;
        module.getVariables().forEach(o -> backEnd.datas.add(MipsData.create(o, mapper)));
        module.getFunctions().forEach(o -> mapper.putLabel(o, new MipsFunction(o.getPureName())));
        module.getFunctions().forEach(o -> backEnd.functions.add(MipsFunction.create(o, mapper.copy())));
        return backEnd;
    }

    public BackEnd allocReg() {
        Allocator allocator = new Allocator(functions, MipsRegs.forAlloc(), mapper);
        allocator.allocate();
        mapper.updateFuncSave();
        return this;
    }

}
