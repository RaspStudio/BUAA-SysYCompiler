package backend.value.meta;

public abstract class MipsLabel {
    protected final String name;

    protected MipsLabel(String name) {
        this.name = name;
    }

    public final String label() {
        return name;
    }
}
