package backend.value.data;

public class MipsString extends MipsData {
    private final String data;

    protected MipsString(String name, String data) {
        super(name);
        this.data = data;
    }

    @Override
    public String toString() {
        return label() + ":\n\t.asciiz\t\"" + data + "\"";
    }
}
