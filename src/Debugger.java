import backend.BackEnd;
import frontend.FrontEnd;
import llvmir.Optimizer;
import llvmir.tree.Module;
import util.IOTools;

import java.io.IOException;
import java.util.Scanner;

import static util.IOTools.writeAndClose;

public class Debugger {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("exit")) {
                break;
            }
            autoTest(IOTools.readAll(line));
            System.out.println("Done");
        }
    }

    public static void autoTest(String source) {
        FrontEnd frontEnd = new FrontEnd(source);
        Optimizer optimizer = new Optimizer(frontEnd.buildIR());
        Module optimized = optimizer.optimized();
        writeAndClose(Compiler.LLVMIR_PATH, optimized.toString());
        BackEnd backEnd = BackEnd.build(optimized).allocReg();
        writeAndClose(Compiler.MIPS_PATH, backEnd.toString());
    }
}

