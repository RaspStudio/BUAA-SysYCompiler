package llvmir;

import llvmir.pass.Pass;
import llvmir.pass.deadcode.DeleteDead;
import llvmir.pass.gvn.GlobalCodeMove;
import llvmir.pass.gvn.GlobalValueNumbering;
import llvmir.pass.mem2reg.MemToReg;
import llvmir.pass.mem2reg.RemovePhi;
import llvmir.tree.Module;

import java.util.Arrays;
import java.util.List;

public class Optimizer {
    private final Module module;
    private boolean optimized = false;
    private final List<Pass> passes = Arrays.asList(
            new GlobalValueNumbering(),
            new DeleteDead(),
            new GlobalCodeMove()
    );

    public Optimizer(Module module) {
        this.module = module;
    }

    public Module original() {
        return module;
    }

    public Module optimized() {
        if (!optimized) {
            optimized = true;
            new MemToReg().pass(module);
            boolean changed;
            do {
                changed = false;
                for (Pass pass : passes) {
                    changed |= pass.runPass(module);
                }
            } while (changed);
            new RemovePhi().pass(module);
        }
        return module;
    }
}
