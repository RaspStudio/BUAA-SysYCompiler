package backend.regalloc;

/**
 *  图着色寄存器分配的寄存器单元接口
 */
public interface VReg extends Comparable<VReg> {

    /**
     * 为寄存器着色，预着色或是寄存器分配时着色
     * @param color 寄存器颜色编码，需要根据具体的架构去定义
     */
    void addColor(int color);

    boolean hasColor();

    int getColor();

    /**
     * 该寄存器被溢出的优先级（优先级高的排在末尾优先溢出）
     * @return 返回寄存器的颜色
     */
    int spillPriority();

    int id();

    /**
     * 用于比较两个寄存器的优先级
     * @param o 另一个寄存器
     * @return 参数中的寄存器应该放在前面返回正数，否则返回负数
     */
    @Override
    default int compareTo(VReg o) {
        if (spillPriority() == o.spillPriority()) {
            return id() - o.id();
        }
        return spillPriority() - o.spillPriority();
    }

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    boolean needAnalyze();
}
