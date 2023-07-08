package frontend.tree.var;

import frontend.exception.Handler;
import frontend.label.LabelList;
import frontend.syntax.factories.SyntaxType;
import frontend.syntax.meta.ParseSyntax;
import frontend.token.meta.SymbolToken;
import frontend.tree.exp.ExpNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class InitValNode {
    private final boolean isUndefined;// 未定义
    private final boolean isArray;// 是数组不是常数
    private final boolean isConst;// 是常数不是数组
    private final int value;
    private final ExpNode exp;
    private final List<InitValNode> array;

    private InitValNode(int val) {
        this.isConst = true;
        this.isArray = false;
        this.value = val;
        this.exp = null;
        this.array = null;
        isUndefined = false;
    }

    private InitValNode(ExpNode exp) {
        this.isConst = false;
        this.isArray = false;
        this.value = 0;
        this.exp = exp;
        this.array = null;
        isUndefined = false;
    }

    private InitValNode(List<InitValNode> array) {
        this.isConst = false;
        this.isArray = true;
        this.value = 0;
        this.exp = null;
        this.array = array;
        isUndefined = false;
    }

    private InitValNode(int val, boolean isUndefined) {
        this.isConst = true;
        this.isArray = false;
        this.value = val;
        this.exp = null;
        this.array = null;
        this.isUndefined = isUndefined;
    }

    private InitValNode(List<InitValNode> array, boolean isUndefined) {
        this.isConst = false;
        this.isArray = true;
        this.value = 0;
        this.exp = null;
        this.array = array;
        this.isUndefined = isUndefined;
    }
    
    public int getValue() {
        return getValue(null);
    }

    public int getValue(List<Integer> dimensions) {
        if (!isArray && isConst) {
            return value;
        } else if (isArray) {
            List<Integer> subDimensions = new ArrayList<>();
            for (int i = 1; i < dimensions.size(); i++) {
                subDimensions.add(dimensions.get(i));
            }
            assert array != null;
            return array.get(dimensions.get(0)).getValue(subDimensions);
        } else {
            throw new RuntimeException("Try Get ConstVal From Not Const Exp");
        }
    }

    public ExpNode getExp() {
        return getExp(Collections.emptyList());
    }

    public ExpNode getExp(List<Integer> dimensions) {
        if (isConst) {
            throw new RuntimeException("Redesign! Try Get Exp From Const");
        } else if (dimensions.size() > 0 && isArray) {
            assert array != null;
            return array.get(dimensions.get(0)).getExp(dimensions.subList(1, dimensions.size()));
        } else if (!isArray) {
            return exp;
        } else {
            throw new RuntimeException("Redesign! Try Get Exp From Array");
        }
    }

    public boolean isConst() {
        return isConst;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isUndefined() {
        return isUndefined;
    }

    protected List<Integer> check() {
        List<Integer> ret = new ArrayList<>();
        if (isArray) {
            assert array != null;
            ret.add(array.size());
            array.forEach(o -> {
                if (!o.check().equals(array.get(0).check())) {
                    throw new RuntimeException("Invalid Init Type!");
                }
            });
            ret.addAll(array.get(0).check());
        }
        return ret;
    }

    public void forEach(Consumer<InitValNode> func) {
        if (!isArray) {
            throw new RuntimeException("Not Array Type");
        }
        assert array != null;
        array.forEach(func);
    }

    public static InitValNode treeify(ParseSyntax syntax, boolean isConst, LabelList labels, Handler handler) {
        if (syntax.get(0).isType(SyntaxType.Wrapper) && syntax.getToken(0).equals(SymbolToken.LBRACESYM)) {
            List<InitValNode> array = new ArrayList<>();
            int i = 1;
            while (i < syntax.size()) {
                array.add(treeify((ParseSyntax) syntax.get(i), isConst, labels, handler));
                i += 2;
            }
            return new InitValNode(array);
        } else {
            return isConst ?
                    new InitValNode(ExpNode.constTreeify((ParseSyntax) syntax.get(0), labels, handler)) :
                    new InitValNode(ExpNode.treeify((ParseSyntax) syntax.get(0), labels, handler));
        }
    }

    public static InitValNode undefined(List<Integer> dimensions) {
        if (dimensions.size() == 0) {
            return UNDEFINED;
        } else {
            List<Integer> subDimensions = new ArrayList<>();
            for (int i = 1; i < dimensions.size(); i++) {
                subDimensions.add(dimensions.get(i));
            }
            List<InitValNode> array = new ArrayList<>();
            if (dimensions.get(0) == null) {
                return UNDEFINED;
            }
            for (int i = 0; i < dimensions.get(0); i++) {
                array.add(undefined(subDimensions));
            }
            return new InitValNode(array, true);
        }
    }

    private static final InitValNode UNDEFINED = new InitValNode(0, true);

}
