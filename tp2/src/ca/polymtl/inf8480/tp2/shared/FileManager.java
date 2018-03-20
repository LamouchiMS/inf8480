package ca.polymtl.inf8480.tp2.shared;

import java.io.IOException;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileManager {
    public static String readFile(String fileName) {
        String content = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            content = sb.toString();

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public static void appendToFile(String filepath, String content) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(filepath, true);
            bw = new BufferedWriter(fw);
            bw.write(content + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}