package backend.value.inst.rtype;

import backend.value.MipsBlock;
import backend.value.meta.MipsImm;
import backend.value.meta.MipsReg;

import java.math.BigInteger;
import java.util.StringJoiner;

public class MipsDiv extends MipsRInst {

    public MipsDiv(MipsBlock parent, MipsReg dest, MipsReg lop, MipsReg rop) {
        super(parent, dest, lop, rop);
    }

    public MipsDiv(MipsBlock parent, MipsReg dest, MipsReg lop, MipsImm rimm) {
        super(parent, dest, lop, rimm);
    }

    @Override
    public String toString() {
        if (isImmType) {
            return optTruncate(dest, lop, rimm);
        } else {
            return super.toString();
        }
    }

    @Override
    protected String getOpcode() {
        return "div";
    }


    /*========== 乘除优化 ==========*/
    private static long mhigh;
    private static long shpost;
    private static int l;

    private static void chooseMultiplier(long sd) {
        final int prec = 31;
        BigInteger d = BigInteger.valueOf(Math.abs(sd));
        int l = (int) Math.ceil(Math.log(d.intValue()) / Math.log(2));
        int shpost = l;
        long mlow = BigInteger.ONE.shiftLeft(32 + l).divide(d).longValue();
        long mhigh = BigInteger.ONE.shiftLeft(32 + l)
                .add(BigInteger.ONE.shiftLeft(32 + l - prec)).divide(d).longValue();
        while (mlow / 2 < mhigh / 2 && shpost > 0) {
            mlow /= 2;
            mhigh /= 2;
            shpost--;
        }
        MipsDiv.mhigh = mhigh;
        MipsDiv.shpost = shpost;
        MipsDiv.l = l;
    }

    private static String optTruncate(MipsReg dest, MipsReg lop, MipsImm rimm) {
        final StringJoiner joiner = new StringJoiner("\n\t", "",
                rimm.value() < 0 ? String.format("\n\tsubu\t\t%s, $zero, %s", dest, dest) : "");
        final int N = 32;
        int d = Math.abs(rimm.value());
        chooseMultiplier(d);
        if (d == 1) {
            return String.format("addu\t\t%s, %s, $zero\t# DIV-OPT-1", dest, lop);
        } else if (Math.abs(d) == BigInteger.valueOf(2).pow(l).longValue()) {
            // t2 = sra(n, l-1)
            joiner.add(String.format("sra \t\t$at, %s, %d", lop, l - 1));
            // t1 = srl(t2, N-l)
            joiner.add(String.format("srl \t\t$at, $at, %d", N - l));
            // t0 = n + t1
            joiner.add(String.format("addu\t\t$at, $at, %s", lop));
            // res = sra(t0, l)
            joiner.add(String.format("sra \t\t%s, $at, %d", dest, l));
            return joiner.toString();
        } else if (mhigh < BigInteger.valueOf(2).pow(N - 1).longValue()) {
            // t0 = hi(n * m)
            joiner.add(String.format("li  \t\t$at, %d", mhigh));
            joiner.add(String.format("mult\t\t%s, $at", lop));
            joiner.add(String.format("mfhi\t\t$at"));
            // t1 = sra(t0, shpost)
            joiner.add(String.format("sra \t\t$v0, $at, %d", shpost));
            // t2 = -xsign(n)
            joiner.add(String.format("slti\t\t$at, %s, 0", lop));
            // res = t1 + t2
            joiner.add(String.format("addu\t\t%s, $v0, $at", dest));
            return joiner.toString();
        } else {
            int mMinusTwoPowN = (int) (mhigh - (1L << N));
            // t0 = hi(n * mMinusTwoPowN)
            joiner.add(String.format("li  \t\t$at, %d", mMinusTwoPowN));
            joiner.add(String.format("mult\t\t$at, %s", lop));
            joiner.add(String.format("mfhi\t\t$at"));
            // t1 = n + t0
            joiner.add(String.format("addu\t\t$v0, %s, $at", lop));
            // t2 = sra(t1, shpost)
            joiner.add(String.format("sra \t\t$v0, $v0, %d", shpost));
            // t3 = -xsign(n)
            joiner.add(String.format("slti\t\t$at, %s, 0", lop));
            // res = t2 + t3
            joiner.add(String.format("addu\t\t%s, $v0, $at", dest));
            return joiner.toString();
        }
    }

    private static String opt(MipsReg dest, MipsReg lop, MipsImm rimm) {
        StringJoiner joiner = new StringJoiner("\n\t", "",
                rimm.value() < 0 ? String.format("\n\tsubu\t\t%s, $zero, %s", dest, dest) : "");
        final int N = 32;
        int d = Math.abs(rimm.value());
        chooseMultiplier(d);
        if (Math.abs(d) == BigInteger.valueOf(2).pow(l).longValue()) {
            // res = sra(n, l)
            joiner.add(String.format("sra \t\t%s, %s, %d", dest, lop, l));
            return joiner.toString();
        } else if (mhigh < BigInteger.valueOf(2).pow(N).longValue()) {
            // nsign($a0) = xsign(n)
            joiner.add(String.format("sra \t\t$a0, %s, %d", lop, N - 1));
            // t1(at) = xor(nsign, n)
            joiner.add(String.format("xor \t\t$at, $a0, %s", lop));
            // q0(at) = hi(mhign * xor(nsign, n))
            joiner.add(String.format("li  \t\t$v0, %d", mhigh));
            joiner.add(String.format("multu\t\t$v0, $at"));
            joiner.add(String.format("mfhi\t\t$at"));
            // t3(at) = srl(q0, shpost)
            joiner.add(String.format("srl \t\t$at, $at, %d", shpost));
            // t4 = xor(t3, t0)
            joiner.add(String.format("xor \t\t%s, $at, $a0", dest));
            return joiner.toString();
        } else {
            return "div\t\t" + dest + ", " + lop + ", " + rimm;
        }
    }
}

