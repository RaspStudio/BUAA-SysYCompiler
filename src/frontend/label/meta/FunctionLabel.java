package frontend.label.meta;

import frontend.token.meta.KeyWordToken;
import frontend.tree.function.FuncFParamNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FunctionLabel extends Label {
    private final VarLabel returnType;
    private final List<VarLabel> fakeParams;

    public FunctionLabel(String name, KeyWordToken type, List<FuncFParamNode> fakeParams) {
        super(name, type.equals(KeyWordToken.INTTK) ? LabelType.IntegerFunc : LabelType.VoidFunc);
        this.fakeParams = new ArrayList<>();
        fakeParams.forEach(o -> this.fakeParams.add(o.label()));
        this.returnType = new VarLabel("return@" + name, type, new ArrayList<>());
    }

    public int size() {
        return fakeParams.size();
    }

    public boolean isValid(List<VarLabel> paramLabels) {
        for (int i = 0; i < paramLabels.size(); i++) {
            if (!fakeParams.get(i).sameTypeWith(paramLabels.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean sameTypeWith(Label label) {
        return label.sameTypeWith(returnType);
    }

    public VarLabel returnLabel() {
        return returnType;
    }

    @Override
    public boolean isBase(KeyWordToken type) {
        return returnType.isBase(type);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FunctionLabel &&
                Objects.equals(returnType, ((FunctionLabel)o).returnType) &&
                Objects.equals(fakeParams, ((FunctionLabel)o).fakeParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, fakeParams);
    }
}
