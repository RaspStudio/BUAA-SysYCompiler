package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;

import java.math.BigInteger;
import java.util.StringJoiner;

public class MipsMul extends MipsRInst {

    public MipsMul(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop) {
        super(parent, dest, lop, rop);
    }

    public MipsMul(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm) {
        super(parent, dest, lop, rimm);
    }

    @Override
    public String toString() {
        if (isImmType && BigInteger.valueOf(Math.abs(rimm.value())).bitCount() == 1) {
            StringJoiner joiner = new StringJoiner("\n\t", "",
                    rimm.value() < 0 ? String.format("\n\tsubu\t\t%s, $zero, %s", dest, dest) : "");
            joiner.add(String.format("sll\t\t%s, %s, %d", dest, lop,
                    BigInteger.valueOf(Math.abs(rimm.value())).bitLength() - 1));
            return joiner.toString();
        } else {
            return super.toString();
        }
    }

    @Override
    protected String getOpcode() {
        return "mul";
    }
}
