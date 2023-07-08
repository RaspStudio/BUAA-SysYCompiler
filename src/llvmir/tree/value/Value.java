package llvmir.tree.value;

import llvmir.tree.type.Type;
import llvmir.tree.type.VoidType;
import llvmir.tree.value.user.User;
import llvmir.tree.value.user.constant.Constant;
import llvmir.tree.value.user.instruction.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Value implements Comparable<Value> {
    protected final int id;
    protected final Type valType;
    protected final List<User> users;
    protected final String name;

    private static int usedId = -1;
    private static final Set<String> usedName = new HashSet<>();

    protected Value(Type valType, String name) {
        this.id = ++usedId;
        this.valType = valType;
        this.users = new ArrayList<>();
        if (this instanceof Constant || this.valType instanceof VoidType) {
            this.name = name;
        } else {
            this.name = usedName.contains(name) ? name + "." + id : name;
            usedName.add(this.name);
        }
    }

    public final String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public final String getPureName() {
        return name.replaceAll("[@%]", "");
    }

    public final Type getType() {
        return valType;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Value && ((Value) o).id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public abstract String toString();

    @Override
    public int compareTo(Value o) {
        return o.users.size() - users.size();
    }

    public void replaceSelfWith(Value nodeValue) {
        users.forEach(user -> user.replaceUse(this, nodeValue));
    }

    public final void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }

    protected void delUser(Instruction value) {
        if (!users.remove(value)) {
            throw new RuntimeException("No Such User!");
        }
    }

    /*---------- 对象ID分配 ----------*/
    public enum Region { Default, ConstArray, LOr, LAnd, Eq, Rel, Branch, Loop }

    private static final Map<String, Integer> allocator = new HashMap<>();

    public static int allocId() {
        return allocId(Region.Default);
    }

    public static int allocId(String region) {
        return allocator.merge(region, 1, Integer::sum);
    }

    public static int allocId(Region region) {
        return allocator.merge(region.name(), 1, Integer::sum);
    }
}
