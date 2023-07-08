package llvmir.pass.mem2reg;

import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.global.Function;
import llvmir.tree.value.user.instruction.*;

import java.util.List;
import java.util.stream.Collectors;

public class RemovePhi extends Pass {

    private List<PhiInst> collectPhi(Function f) {
        return f.getBlocks().stream()
                .flatMap(b -> b.getAllInstructions().stream())
                .filter(i -> i instanceof PhiInst)
                .map(i -> (PhiInst) i)
                .collect(Collectors.toList());
    }

    private void removePhiInFunction(Function function) {
        // 简单版本，把 Phi 指令改为存取内存
        BasicBlock entrance = function.getBlocks().get(0);
        collectPhi(function).forEach(inst -> {
            // 分配空间
            AllocaInst alloca = new AllocaInst(inst.getType(), inst.getPureName() + ".phi", entrance);
            entrance.insertHead(alloca);
            // 在前驱块存储
            inst.getIncoming().forEach(p -> {
                Value value = p.getLeft();
                BasicBlock from = p.getRight();
                if (value instanceof Instruction) {
                    if (((Instruction) value).getParent() == from) {
                        from.insertAfter((Instruction) value, new StoreInst(from, value, alloca));
                    } else {
                        from.insertHead(new StoreInst(from, value, alloca));
                    }
                } else {
                    from.insertTail(new StoreInst(from, value, alloca));
                }
            });
            // 在 Phi 指令所在块加载
            LoadInst load = new LoadInst(inst.getParent(), alloca);
            inst.replaceSelfWith(load);
            inst.getParent().insertAfter(inst, load);
            // 删除 Phi 指令
            inst.getParent().removeInst(inst);
        });
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(this::removePhiInFunction);
    }
}
