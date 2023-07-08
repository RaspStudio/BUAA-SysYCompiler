package backend.translate;

import backend.value.MipsBlock;
import backend.value.MipsFunction;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsLabel;
import backend.value.meta.MipsReg;
import llvmir.tree.value.Argument;
import llvmir.tree.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在MipsMapper中，我们将LLVM的IR中的Value映射到Mips的寄存器中。
 * 在中端的实现中，IR Value 实体有：
 *     1. Argument : 函数的参数（虚拟寄存器类型）
 *     2. BasicBlock : 基本块（标签地址类型）
 *     3. Function : 函数（标签地址类型）
 *     4. GlobalVariable : 全局变量（内存地址类型）
 *     5. Instruction : 指令（虚拟寄存器类型）
 *     6. ConstantInt : 常量整数（立即数类型）
 */
public class MipsMapper {
    private final Map<Value, MipsReg> valueToReg = new HashMap<>();
    private final Map<Value, MipsAddr> valueToAddr = new HashMap<>();
    private final Map<Value, MipsAddr> argToAddr = new HashMap<>();
    private final Map<Value, MipsLabel> valueToLabel = new HashMap<>();
    private final Map<MipsFunction, List<MipsFunction>> funcToSubFunc;
    private final Map<MipsFunction, List<Integer>> funcToSave;

    public MipsMapper() {
        funcToSubFunc = new HashMap<>();
        funcToSave = new HashMap<>();
    }

    private MipsMapper(Map<MipsFunction, List<MipsFunction>> subFuncs, Map<MipsFunction, List<Integer>> funcToSave) {
        this.funcToSubFunc = subFuncs;
        this.funcToSave = funcToSave;
    }

    public MipsMapper copy() {
        MipsMapper mapper = new MipsMapper(funcToSubFunc, funcToSave);
        mapper.valueToReg.putAll(valueToReg);
        mapper.valueToAddr.putAll(valueToAddr);
        mapper.valueToLabel.putAll(valueToLabel);
        return mapper;
    }

    /* 虚拟寄存器类型 : 虚拟寄存器 */
    public MipsReg getReg(Value value) {
        if (valueToReg.containsKey(value)) {
            return valueToReg.get(value);
        } else {
            throw new RuntimeException("Value " + value + " has no corresponding register");
        }
    }

    public void putReg(Value value, MipsReg reg) {
        valueToReg.put(value, reg);
    }

    /* 虚拟地址类型 : 指针类型的虚拟寄存器 */
    public MipsAddr getAddr(Value value) {
        if (valueToAddr.containsKey(value)) {
            return valueToAddr.get(value);
        } else {
            throw new RuntimeException("Value " + value + " has no corresponding address");
        }
    }

    public void putAddr(Value value, MipsAddr addr) {
        valueToAddr.put(value, addr);
    }

    /* 虚拟标签类型 : 函数和基本块 */
    public MipsFunction getFunction(Value value) {
        if (valueToLabel.containsKey(value)) {
            MipsLabel label = valueToLabel.get(value);
            if (label instanceof MipsFunction) {
                return (MipsFunction) label;
            } else {
                throw new RuntimeException("Value " + value + " is not a function");
            }
        } else {
            throw new RuntimeException("Value " + value + " has no corresponding function");
        }
    }

    public MipsBlock getBlock(Value value) {
        if (valueToLabel.containsKey(value)) {
            MipsLabel label = valueToLabel.get(value);
            if (label instanceof MipsBlock) {
                return (MipsBlock) label;
            } else {
                throw new RuntimeException("Value " + value + " is not a block");
            }
        } else {
            throw new RuntimeException("Value " + value + " has no corresponding block");
        }
    }

    public void putLabel(Value value, MipsLabel label) {
        valueToLabel.put(value, label);
    }

    /* 参数类型 : 暂时是唯一特殊的类型，在 IR 中是虚拟寄存器，在此处全部在内存中 */
    public MipsAddr getArgAddr(Argument value) {
        if (argToAddr.containsKey(value)) {
            return argToAddr.get(value);
        } else {
            throw new RuntimeException("Argument " + value + " has no corresponding address");
        }
    }

    public void putArgPointer(Argument argument, MipsAddr addr) {
        argToAddr.put(argument, addr);
    }

    /* 函数类型 : 保存的寄存器 */
    public List<Integer> getSaveRegs(MipsFunction function) {
        if (funcToSave.containsKey(function)) {
            return funcToSave.get(function);
        } else {
            throw new RuntimeException("Function " + function + " has no corresponding save registers");
        }
    }

    public void putSaveRegs(MipsFunction function, List<Integer> regs) {
        funcToSave.put(function, regs);
    }

    /* 函数类型 : 调用的子函数 */
    public void putSubFunc(MipsFunction function, MipsFunction subFunc) {
        funcToSubFunc.putIfAbsent(function, new ArrayList<>());
        funcToSubFunc.get(function).add(subFunc);
    }

    public void updateFuncSave() {
        for (MipsFunction function : funcToSubFunc.keySet()) {
            // 对每个 Caller 函数，遍历其所有的 Callee 函数，找到最大的 Callee 函数的保存寄存器
            Set<Integer> saveRegs = new HashSet<>();
            for (MipsFunction subFunc : funcToSubFunc.get(function)) {
                saveRegs.addAll(getSaveRegs(subFunc));
            }
            // 此时，saveRegs 中保存的是所有的 Callee 函数的保存寄存器的并集
            function.stack().updateFuncSaveCnt(saveRegs.size());
        }
    }

}
