package backend.regalloc;

import java.util.List;

public interface VBlock<I extends VInst<R>, R extends VReg, B extends VBlock<I, R, B>> {
    List<I> getInsts();

    /**
     * 获得当前基本块的前驱基本块
     * @return 前驱基本块列表
     */
    List<B> getPreds();

    /**
     * 获得当前基本块的后继基本块
     * @return 后继基本块列表
     */
    List<B> getSuccs();
}
