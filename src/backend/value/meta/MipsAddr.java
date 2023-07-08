package backend.value.meta;

public class MipsAddr {
    private final MipsLabel label;
    private final MipsImm offset;
    private final MipsReg base;

    protected MipsAddr(MipsLabel label, MipsImm offset, MipsReg base) {
        this.label = label;
        this.offset = offset;
        this.base = base;
    }

    public static MipsAddr of(MipsLabel label, MipsImm offset, MipsReg base) {
        return new MipsAddr(label, offset, base);
    }

    public MipsReg getBase() {
        return base;
    }

    public MipsAddr replaceBase(MipsReg newValue) {
        return new MipsAddr(label, offset, newValue);
    }

    public MipsAddr add(int offset) {
        return new MipsAddr(label, this.offset == null ? MipsImm.of(offset) : this.offset.add(offset), base);
    }

    @Override
    public String toString() {
        return label != null && offset == null && base == null ? label.label() :
                String.format(
                    "%s%s%s",
                    label == null ? "" : label.label() + " + ",
                    offset == null ? "0" : offset.toString(),
                    base == null ? "" : "(" + base + ")"
                );
    }

}
