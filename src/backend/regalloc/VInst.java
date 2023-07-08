package backend.regalloc;

import java.util.Collection;
import java.util.List;

public interface VInst<R extends VReg> {

    List<R> getDefs();

    List<R> getUses();

    /**
     * 在给定的基本块中，分析指令的使用和定义，将结果存入参数提供的集合中
     * @param analyzed 基本块中已经分析过的寄存器集合
     * @param defs 基本块先定义的寄存器集合
     * @param uses 基本块先使用的寄存器集合
     */
    default void analyze(Collection<R> analyzed, Collection<R> defs, Collection<R> uses) {
        for (R def : getDefs()) {
            if (def == null) {
                throw new Error("def is null");
            }
            if (!analyzed.contains(def) && def.needAnalyze()) {
                analyzed.add(def);
                defs.add(def);
            }
        }
        for (R use : getUses()) {
            if (use == null) {
                throw new Error("use is null");
            }
            if (!analyzed.contains(use) && use.needAnalyze()) {
                analyzed.add(use);
                uses.add(use);
            }
        }
    }

    void replaceUse(R old, R newValue);

}
