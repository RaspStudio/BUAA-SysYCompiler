import backend.BackEnd;
import frontend.FrontEnd;
import frontend.exception.FrontEndException;
import llvmir.Optimizer;
import util.IOTools;

import java.io.IOException;

import static util.IOTools.writeAndClose;

public class Compiler {
    public static final String INPUT_PATH = "testfile.txt";
    public static final String OUTPUT_PATH = "output.txt";
    public static final String ERROR_PATH = "error.txt";
    public static final String LLVMIR_PATH = "llvm_ir.txt";
    public static final String MIPS_PATH = "mips.txt";

    public static void main(String[] args) throws IOException {
        //dumpIROpt();
        //debugMips();
        //debugMipsOpt();
        mipsOpt();
    }

    /*---------- 功能接口 ----------*/
    public static void parseAnalysis() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        try {
            writeAndClose(OUTPUT_PATH, frontEnd.parsedString());
        } catch (FrontEndException e) {
            e.printStackTrace();
        }
    }

    public static void exceptionAnalysis() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        try {
            writeAndClose(ERROR_PATH, frontEnd.exceptionString());
        } catch (FrontEndException e) {
            e.printStackTrace();
        }
    }

    public static void dumpIR() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        writeAndClose(LLVMIR_PATH, frontEnd.buildIR().toString());
    }

    private static void debugIR() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        System.out.println(frontEnd.buildIR());
    }

    public static void mipsOpt() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        Optimizer optimizer = new Optimizer(frontEnd.buildIR());
        BackEnd backEnd = BackEnd.build(optimizer.optimized()).allocReg().peepHole();
        writeAndClose(MIPS_PATH, backEnd.toString());
    }

    public static void dumpIROpt() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        Optimizer optimizer = new Optimizer(frontEnd.buildIR());
        writeAndClose(LLVMIR_PATH, optimizer.optimized().toString());
    }

    public static void debugMips() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        writeAndClose(LLVMIR_PATH, frontEnd.buildIR().toString());
        Optimizer optimizer = new Optimizer(frontEnd.buildIR());
        BackEnd backEnd = BackEnd.build(optimizer.original()).allocReg();
        writeAndClose(MIPS_PATH, backEnd.toString());
    }

    public static void debugMipsOpt() throws IOException {
        FrontEnd frontEnd = new FrontEnd(IOTools.readAll(INPUT_PATH));
        Optimizer optimizer = new Optimizer(frontEnd.buildIR());
        BackEnd original = BackEnd.build(optimizer.original()).allocReg();
        writeAndClose("_" + MIPS_PATH, original.toString());
        writeAndClose(LLVMIR_PATH, optimizer.optimized().toString());
        BackEnd backEnd = BackEnd.build(optimizer.optimized()).allocReg().peepHole();
        writeAndClose(MIPS_PATH, backEnd.toString());
    }

}
