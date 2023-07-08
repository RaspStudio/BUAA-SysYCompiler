package frontend.label.meta;

import frontend.token.meta.KeyWordToken;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValueType {
    // 基本类型
    private final KeyWordToken base;
    // 维度（0维为单个变量，其余维度为数组，1维仅形参可以为null代表留空）
    private final List<Integer> dimensions;

    protected ValueType(KeyWordToken base, List<Integer> dimensions) {
        this.base = base;
        this.dimensions = dimensions;
    }

    public Pair<KeyWordToken, List<Integer>> getType() {
        return new Pair<>(base, new ArrayList<>(dimensions));
    }

    public ValueType subType(int dive) {
        List<Integer> subTypeDimensions = new ArrayList<>();
        for (int i = dive; i < this.dimensions.size(); i++) {
            subTypeDimensions.add(this.dimensions.get(i));
        }
        return new ValueType(base, subTypeDimensions);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ValueType && ((ValueType) o).base.equals(base)
                && ((ValueType) o).dimensions.size() == dimensions.size();
    }

    @Override
    public int hashCode() {
        return Objects.hash(base.getContent(), dimensions);
    }

    public boolean isBase(KeyWordToken type) {
        return type.equals(base);
    }
}
