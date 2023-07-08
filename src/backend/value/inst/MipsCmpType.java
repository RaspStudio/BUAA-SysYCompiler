package backend.value.inst;

import llvmir.tree.value.user.instruction.binary.ICmpInst;

public enum MipsCmpType {
    EQ("beq", "seq"),
    NE("bne", "sne"),
    LT("blt", "slt"),
    LE("ble", "sle"),
    GT("bgt", "sgt"),
    GE("bge", "sge"),
    EQZ("beqz"),
    NEZ("bnez"),
    GEZ("bgez"),
    GTZ("bgtz"),
    LEZ("blez"),
    LTZ("bltz");

    private final String branch;
    private final String cmp;
    private final boolean isBinary;

    MipsCmpType(String branchOp, String cmpOp) {
        this.branch = branchOp;
        this.cmp = cmpOp;
        this.isBinary = true;
    }

    MipsCmpType(String branchOp) {
        this.branch = branchOp;
        this.cmp = null;
        this.isBinary = false;
    }

    public static MipsCmpType of(ICmpInst.CmpType cmpType) {
        switch (cmpType) {
            case EQ: return EQ;
            case NE: return NE;
            case SGE: return GE;
            case SGT: return GT;
            case SLE: return LE;
            case SLT: return LT;
            default: throw new RuntimeException("Invalid CmpType");
        }
    }

    public String getBranch() {
        return (branch + "    ").substring(0, 4);
    }

    public String getCmp() {
        return cmp;
    }
}
