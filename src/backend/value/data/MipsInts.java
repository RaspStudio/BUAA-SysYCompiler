package backend.value.data;

import java.util.List;
import java.util.StringJoiner;

public class MipsInts extends MipsData {
    private final List<Integer> data;
    private final boolean useSpace;

    protected MipsInts(String name, List<Integer> data) {
        super(name);
        this.data = data;

        boolean hasNonZero = false;
        for (Integer i : data) {
            if (i != 0) {
                hasNonZero = true;
                break;
            }
        }
        this.useSpace = !hasNonZero;
    }

    @Override
    public String toString() {
        if (useSpace) {
            return ".align 2\n" + label() + ":\n\t.space " + data.size() * 4;
        } else {
            StringJoiner joiner = new StringJoiner("\n\t.word\t");
            joiner.add(".align 2\n" + label() + ":");
            data.forEach(o -> joiner.add(o.toString()));
            return joiner.toString();
        }
    }
}
