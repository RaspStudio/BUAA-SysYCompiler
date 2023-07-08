package util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class IOTools {
    public static String readAll(String path) throws IOException {
        FileReader in = new FileReader(path);
        StringBuilder source = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            source.append((char)c);
        }
        in.close();
        return source.toString();
    }

    public static List<String> readLines(String path) throws IOException {
        Scanner scanner = new Scanner(path);
        List<String> ret = new ArrayList<>();
        while (scanner.hasNextLine()) {
            ret.add(scanner.nextLine().trim());
        }
        ret.removeIf(o -> o.trim().length() == 0);
        return ret;
    }

    public static void writeAndClose(String path, String s) {
        try (FileWriter out = new FileWriter(path)) {
            out.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
