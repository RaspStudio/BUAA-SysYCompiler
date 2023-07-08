package backend.value.meta;

import backend.regalloc.VReg;
import backend.value.inst.MipsInst;

import java.util.ArrayList;
import java.util.List;

public class MipsReg implements VReg {
    protected static int counter = 0;
    protected final int id = counter++;
    protected final String name;
    protected MipsInst def;
    protected final List<MipsInst> users = new ArrayList<>();
    private int color = -1;

    public MipsReg(String name) {
        this.name = name;
    }

    @Override
    public void addColor(int color) {
        if (this.color == -1) {
            this.color = color;
        } else if (color != this.color) {
            throw new RuntimeException("Cannot Set Color Twice");
        }
    }

    public void setDef(MipsInst def) {
        if (this.def != null) {
            throw new RuntimeException("Cannot Set Def Twice");
        }
        this.def = def;
    }

    public MipsInst getDef() {
        return def;
    }

    public void delUser(MipsInst user) {
        if (!users.remove(user)) {
            throw new RuntimeException("delUser: " + user + " not found");
        }
    }

    public void delDef(MipsInst user) {
        if (def == null || def != user) {
            throw new RuntimeException("delDef: def is null");
        }
        def = null;
    }

    public void addUser(MipsInst user) {
        users.add(user);
    }

    public List<MipsInst> getUsers() {
        return users;
    }

    @Override
    public boolean hasColor() {
        return color != -1;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public boolean needAnalyze() {
        return !hasColor();//|| MipsRegs.forAlloc().contains(getColor());
    }

    @Override
    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    @Override
    public int spillPriority() {
        return users.size();
    }

    @Override
    public String toString() {
        return hasColor() ? "$" + MipsRegs.of(color) : "<%" + name + ">";
    }

    public static MipsReg of(MipsRegs reg) {
        MipsReg ret = new MipsReg(reg.toString());
        ret.addColor(reg.index());
        return ret;
    }

}
