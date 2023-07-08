package backend.regalloc;

import java.util.List;

public interface VFunc<I extends VInst<R>, R extends VReg, B extends VBlock<I, R, B>> {
    List<B> getBlocks();
}
