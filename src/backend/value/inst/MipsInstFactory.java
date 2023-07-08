package backend.value.inst;

import backend.translate.MipsMapper;
import backend.value.MipsBlock;
import backend.value.inst.atype.MipsLoadAddr;
import backend.value.inst.atype.MipsLoadWord;
import backend.value.inst.atype.MipsSaveWord;
import backend.value.inst.rtype.MipsAdd;
import backend.value.inst.rtype.MipsMul;
import backend.value.inst.rtype.MipsRInst;
import backend.value.inst.special.MipsBranch;
import backend.value.inst.special.MipsFuncCall;
import backend.value.inst.special.MipsJr;
import backend.value.inst.special.MipsJump;
import backend.value.inst.special.MipsMove;
import backend.value.inst.special.MipsSyscall;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;
import llvmir.tree.type.IntegerType;
import llvmir.tree.type.PointerType;
import llvmir.tree.value.Argument;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.constant.global.GlobalVariable;
import llvmir.tree.value.user.instruction.AllocaInst;
import llvmir.tree.value.user.instruction.CallInst;
import llvmir.tree.value.user.instruction.GetElementPtrInst;
import llvmir.tree.value.user.instruction.Instruction;
import llvmir.tree.value.user.instruction.LoadInst;
import llvmir.tree.value.user.instruction.StoreInst;
import llvmir.tree.value.user.instruction.ZExtInst;
import llvmir.tree.value.user.instruction.binary.BinaryOperator;
import llvmir.tree.value.user.instruction.binary.ICmpInst;
import llvmir.tree.value.user.instruction.terminator.BrInst;
import llvmir.tree.value.user.instruction.terminator.RetInst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class MipsInstFactory {
    // 分发指令
    public static List<MipsInst> translate(Instruction inst, MipsBlock parent, MipsMapper mapper) {
        if (inst instanceof BinaryOperator) {
            return translateBinaryOperator((BinaryOperator) inst, parent, mapper);
        } else if (inst instanceof LoadInst) {
            return translateLoadInst((LoadInst) inst, parent, mapper);
        } else if (inst instanceof StoreInst) {
            return translateStoreInst((StoreInst) inst, parent, mapper);
        } else if (inst instanceof AllocaInst) {
            return translateAllocaInst((AllocaInst) inst, parent, mapper);
        } else if (inst instanceof GetElementPtrInst) {
            return translateGetPtrInst((GetElementPtrInst) inst, parent, mapper);
        } else if (inst instanceof ZExtInst) {
            return translateZExtInst((ZExtInst) inst, parent, mapper);
        } else if (inst instanceof CallInst) {
            return translateCallInst((CallInst) inst, parent, mapper);
        } else if (inst instanceof BrInst) {
            return translateBrInst((BrInst) inst, parent, mapper);
        } else if (inst instanceof RetInst) {
            return translateRetInst((RetInst) inst, parent, mapper);
        } else {
            throw new RuntimeException("Unknown instruction type");
        }
    }

    /**
     * 将给定的 IR Value 虚拟寄存器映射到 Mips 寄存器
     * @param value 在 IR 中的虚拟寄存器
     * @param parent 翻译的目标 Mips 块
     * @param mapper 翻译中使用的已有寄存器映射
     * @return 映射的 Mips 寄存器
     */
    private static MipsReg toReg(Value value, MipsBlock parent, MipsMapper mapper, Consumer<MipsInst> submit) {
        List<MipsInst> ret = new ArrayList<>();
        MipsReg retReg;
        if (value instanceof ConstantInt) {
            retReg = new MipsReg(value.getPureName());
            ret.add(new MipsAdd(// todo LoadImmediate
                    parent, retReg, MipsReg.of(MipsRegs.ZERO), MipsImm.of(((ConstantInt) value).getValue())));
        } else if (value instanceof GetElementPtrInst || value instanceof AllocaInst ||
                value instanceof GlobalVariable) {
            retReg = new MipsReg(value.getPureName());
            ret.add(new MipsLoadAddr(parent, retReg, mapper.getAddr(value)));
        } else if (value instanceof Instruction) {
            retReg = mapper.getReg(value);
        } else if (value instanceof Argument) {
            retReg = new MipsReg(value.getPureName());//todo 读取参数暂时全都从内存
            ret.add(new MipsLoadWord(parent, retReg, mapper.getArgAddr((Argument) value)));
        } else  {
            throw new RuntimeException("Unknown value type for reg");
        }
        ret.forEach(submit);
        return retReg;
    }

    /**
     * 将给定的 IR Value 虚拟寄存器的值映射为 Mips 地址
     * 目标虚拟寄存器的类型为指针类型，需要根据数据存储类型进行映射
     * @param value 在 IR 中的虚拟寄存器
     * @param parent 翻译的目标 Mips 块
     * @param mapper 翻译中使用的已有寄存器和地址映射
     * @param submit 翻译过程中产生的指令
     * @return 映射的 Mips 地址
     */
    private static MipsAddr toAddr(Value value, MipsBlock parent, MipsMapper mapper, Consumer<MipsInst> submit) {
        if (value instanceof GetElementPtrInst || value instanceof AllocaInst || value instanceof GlobalVariable) {
            return mapper.getAddr(value);
        } else if (value instanceof Argument) {
            MipsReg realArg = new MipsReg(value.getPureName() + "_reg" + Value.allocId("BACK_ARG_REG"));
            submit.accept(new MipsLoadWord(parent, realArg, mapper.getArgAddr((Argument) value)));
            return MipsAddr.of(null, null, realArg);
        } else if (value instanceof LoadInst) {
            return MipsAddr.of(null, null, toReg(value, parent, mapper, submit));
        } else {
            throw new RuntimeException("Unknown value type for addr");
        }
    }

    // 分发二元指令
    public static List<MipsInst> translateBinaryOperator(BinaryOperator inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        MipsReg dest = new MipsReg(inst.getPureName());
        MipsReg lop = toReg(inst.getOperand(0), parent, mapper, ret::add);
        if (inst.getOperand(1) instanceof ConstantInt &&
                !(inst instanceof ICmpInst && ((ICmpInst) inst).getCmpType().equals(ICmpInst.CmpType.SLT))) {
            MipsImm imm = MipsImm.of(((ConstantInt) inst.getOperand(1)).getValue());
            ret.add(MipsRInst.create(parent, dest, lop, inst, imm));
        } else {
            MipsReg rop = toReg(inst.getOperand(1), parent, mapper, ret::add);
            ret.add(MipsRInst.create(parent, dest, lop, inst, rop));
        }
        mapper.putReg(inst, dest);
        return ret;
    }

    private static List<MipsInst> translateLoadInst(LoadInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        MipsReg dest = new MipsReg(inst.getPureName());
        MipsAddr addr = toAddr(inst.getOperand(0), parent, mapper, ret::add);
        ret.add(new MipsLoadWord(parent, dest, addr));
        mapper.putReg(inst, dest);
        return ret;
    }

    private static List<MipsInst> translateStoreInst(StoreInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        MipsReg src = toReg(inst.getOperand(0), parent, mapper, ret::add);
        MipsAddr addr = toAddr(inst.getOperand(1), parent, mapper, ret::add);
        ret.add(new MipsSaveWord(parent, src, addr));
        return ret;
    }

    private static List<MipsInst> translateAllocaInst(AllocaInst inst, MipsBlock parent, MipsMapper mapper) {
        MipsAddr addr = parent.getParent().stack().allocData((inst.getElementType().size() + 7) / 8);
        mapper.putAddr(inst, addr);
        return Collections.emptyList();
    }

    private static List<MipsInst> translateGetPtrInst(GetElementPtrInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        MipsAddr addr = toAddr(inst.getOperand(0), parent, mapper, ret::add);
        PointerType type = (PointerType) inst.getOperand(0).getType();
        for (int i = 1; i < inst.getOperands().size(); i++) {
            Value operand = inst.getOperand(i);
            if (operand instanceof ConstantInt) {
                // 常量时，直接加上偏移量
                addr = addr.add(((ConstantInt) operand).getValue() * type.getDerivedType(i).byteSize());
            } else {
                // 非常量时，需要先计算偏移量
                // 先把当前地址存到寄存器中
                MipsReg lastAddr = new MipsReg(inst.getPureName() + "_" + i);
                ret.add(new MipsLoadAddr(parent, lastAddr, addr));

                // 计算偏移量
                MipsReg index = toReg(operand, parent, mapper, ret::add);
                MipsReg offset = new MipsReg(inst.getPureName() + "_" + i + "_offset");
                // 单位偏移量为指针指向的元素的大小
                ret.add(new MipsMul(parent, offset, index, MipsImm.of(type.getDerivedType(i).byteSize())));

                // 原地址加上偏移量
                MipsReg newAddr = new MipsReg(inst.getPureName() + "_" + i + "_new");
                ret.add(new MipsAdd(parent, newAddr, lastAddr, offset));

                // 赋值新的地址
                addr = MipsAddr.of(null, MipsImm.of(0), newAddr);
            }
        }

        // 把最终的地址存到映射表中
        mapper.putAddr(inst, addr);
        return ret;
    }

    private static List<MipsInst> translateZExtInst(ZExtInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        mapper.putReg(inst, toReg(inst.getOperand(0), parent, mapper, ret::add));
        if (ret.size() > 0) {
            throw new RuntimeException("没想到的一种情况");
        }
        return ret;
    }

    private static List<MipsInst> translateCallInst(CallInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        if (inst.isBuiltIn()) {
            // 内建函数，内联使用 SysCall 指令。
            if (inst.getOperand(0).getPureName().equals(Function.INPUT_FUNC)) {
                //todo: 直接用v0不清楚会不会有问题（v0的reg对象的引用关系混乱）
                //todo: 将所有移动换成Move指令便于优化
                ret.add(new MipsAdd(parent,
                        MipsReg.of(MipsRegs.V0), MipsReg.of(MipsRegs.ZERO), MipsImm.of(MipsSyscall.GET_INT)));
                ret.add(new MipsSyscall(parent, true));
                MipsReg forRet = new MipsReg(inst.getPureName());
                ret.add(new MipsMove(parent, forRet, MipsReg.of(MipsRegs.V0)));
                mapper.putReg(inst, forRet);
            } else if (inst.getOperand(0).getPureName().equals(Function.OUTPUT_INT_FUNC)) {
                ret.add(new MipsMove(parent,
                        MipsReg.of(MipsRegs.A0), toReg(inst.getOperand(1), parent, mapper, ret::add)));
                ret.add(new MipsAdd(parent,
                        MipsReg.of(MipsRegs.V0), MipsReg.of(MipsRegs.ZERO), MipsImm.of(MipsSyscall.PUT_INT)));
                ret.add(new MipsSyscall(parent, false));
            } else if (inst.getOperand(0).getPureName().equals(Function.OUTPUT_STR_FUNC)) {
                ret.add(new MipsMove(parent,
                        MipsReg.of(MipsRegs.A0), toReg(inst.getOperand(1), parent, mapper, ret::add)));
                ret.add(new MipsAdd(parent,
                        MipsReg.of(MipsRegs.V0), MipsReg.of(MipsRegs.ZERO), MipsImm.of(MipsSyscall.PUT_STR)));
                ret.add(new MipsSyscall(parent, false));
            } else {
                throw new RuntimeException("Unknown built-in function: " + inst.getOperand(0).getPureName());
            }
        } else {
            // 外部函数，保存现场，加载参数，跳转，恢复现场。
            parent.getParent().stack().updateFuncArgCnt(inst.getOperands().size() - 1);
            // 加载参数
            for (int i = 1; i < inst.getOperands().size(); i++) {
                ret.add(new MipsSaveWord(parent,
                        toReg(inst.getOperand(i), parent, mapper, ret::add),
                        parent.getParent().stack().getSubFuncArg(i - 1)));
            }
            // 跳转
            if (inst.getType() instanceof IntegerType) {
                ret.add(new MipsFuncCall(parent, mapper.getFunction(inst.getOperand(0)), mapper, true));
            } else {
                ret.add(new MipsFuncCall(parent, mapper.getFunction(inst.getOperand(0)), mapper,false));
            }
            if (inst.getType() instanceof IntegerType) {
                MipsReg forRet = new MipsReg(inst.getPureName());
                ret.add(new MipsMove(parent, forRet, MipsReg.of(MipsRegs.V0)));
                mapper.putReg(inst, forRet);
            }
            mapper.putSubFunc(parent.getParent(), mapper.getFunction(inst.getOperand(0)));
        }
        return ret;
    }

    private static List<MipsInst> translateBrInst(BrInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        if (inst.getOperands().size() == 1) {
            // 无条件跳转
            ret.add(new MipsJump(parent, mapper.getBlock(inst.getOperand(0))));
        } else {
            // 条件跳转
            MipsReg cond = toReg(inst.getOperand(0), parent, mapper, ret::add);
            ret.add(new MipsBranch(parent, cond, MipsReg.of(MipsRegs.ZERO), MipsCmpType.NE,
                    mapper.getBlock(inst.getOperand(1))));
            ret.add(new MipsJump(parent, mapper.getBlock(inst.getOperand(2))));
        }
        return ret;
    }

    private static List<MipsInst> translateRetInst(RetInst inst, MipsBlock parent, MipsMapper mapper) {
        List<MipsInst> ret = new ArrayList<>();
        // 把返回值存到 v0 中
        if (inst.getOperand(0) != null) {
            ret.add(new MipsMove(parent, MipsReg.of(MipsRegs.V0),
                    toReg(inst.getOperand(0), parent, mapper, ret::add)));
        }
        // 加载返回地址
        ret.add(new MipsLoadWord(parent, MipsReg.of(MipsRegs.RA), parent.getParent().stack().getCurFuncRa()));
        // 加载栈指针
        ret.add(new MipsAdd(parent, MipsReg.of(MipsRegs.SP), MipsReg.of(MipsRegs.SP),
                MipsImm.of(parent.getParent().stack().curStackSize())));
        // 跳转到返回地址
        ret.add(new MipsJr(parent, MipsReg.of(MipsRegs.RA)));
        return ret;
    }
}
