package llvmir.tree.value.user.constant.data;

import llvmir.tree.type.Types;
import util.Pair;

public class ConstantString extends ConstantData {
    private final String content;

    public ConstantString(String str) {
        super(
                Types.array(Types.CHAR, replaceNextLine(str).getLeft()),
                replaceNextLine(str).getRight()
        );
        this.content = str;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return valType + " " + name;
    }

    public static Pair<Integer, String> replaceNextLine(String in) {
        return new Pair<>(
                in.replace("\\n", "$").length() + 1,
                String.format("c\"%s\\00\"", in.replace("\\n", "\\0A"))
        );
    }
}
