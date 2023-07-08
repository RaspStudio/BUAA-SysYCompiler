package backend.value.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum MipsRegs {
    ZERO(0), AT(1), V0(2), V1(3),
    A0(4), A1(5), A2(6), A3(7),
    T0(8), T1(9), T2(10), T3(11),
    T4(12), T5(13), T6(14), T7(15),
    S0(16), S1(17), S2(18), S3(19),
    S4(20), S5(21), S6(22), S7(23),
    T8(24), T9(25), K0(26), K1(27),
    GP(28), SP(29), FP(30), RA(31);

    private final int index;
    MipsRegs(int index) {
        this.index = index;
    }

    public static List<Integer> forAlloc() {
        List<Integer> ret = new ArrayList<>();
        Arrays.asList(
                T0, T1, T2, T3, T4, T5, T6, T7,
                S0, S1, S2, S3, S4, S5, S6, S7,
                T8, T9, K0, K1, GP, FP
        ).forEach(r -> ret.add(r.index()));
        return ret;
    }

    public int index() {
        return index;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static MipsRegs of(int index) {
        for (MipsRegs reg : MipsRegs.values()) {
            if (reg.index == index) {
                return reg;
            }
        }
        throw new IllegalArgumentException("Invalid register number: " + index);
    }
}
