package llvmir.pass;

import llvmir.tree.Module;

public abstract class Pass {
    private boolean changed;

    public final boolean runPass(Module module) {
        changed = false;
        pass(module);
        return changed;
    }

    protected final void setChanged() {
        changed = true;
    }

    protected abstract void pass(Module module);
}
