package backend.value.meta;

import java.util.function.Supplier;

public class MipsImm {
    private final Supplier<Integer> value;

    private MipsImm(Supplier<Integer> value) {
        this.value = value;
    }

    public static MipsImm of(int value) {
        return new MipsImm(() -> value);
    }

    public static MipsImm of(Supplier<Integer> ref) {
        return new MipsImm(ref);
    }

    public int value() {
        return value.get();
    }

    public MipsImm add(int offset) {
        return new MipsImm(() -> value() + offset);
    }

    @Override
    public String toString() {
        return String.valueOf(value.get());
    }

}
