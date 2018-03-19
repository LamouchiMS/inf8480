package ca.polymtl.inf8480.tp2.server;

import java.io.*;
import java.lang.Math;
import ca.polymtl.inf8480.tp2.shared.*;

public class Server implements ServerInterface {
    final int MAX_OPERATIONS = 1000;
    final int MIN_OPERATIONS = 100;
    final int MODULO = 4000;
    private int m = 0; // malice
    private int q = 0; // nbr d'operations acceptable

    public Server() {
        // Initialiser la configuration
        this.m = (int) Math.random() * 100;
        this.q = (int) Math.round(Math.random() * (MAX_OPERATIONS - MIN_OPERATIONS)) + MIN_OPERATIONS;
    }

    public int getQ() {
        return this.q;
    }

    public int calculateSum(String filePath) {
        String content = readFile(filePath);
        String[] lines = content.split(System.lineSeparator());

        int sum = 0;

        for (int i = 0; i < lines.length; i++) {
            String[] parts = lines[i].split("/");
            sum = (sum + this.calculateLine(parts[0], parts[1])) % MODULO;
        }

        int randomVal = (int) Math.random() * 100;

        if (randomVal < this.m) {
            int randomResult = (int) Math.round(Math.random() * Integer.MAX_VALUE);
            return randomResult;
        } else {
            return sum;
        }
    }

    public int calculateLine(String operator, String operand) {
        int op = Integer.parseInt(operand);

        if (operator.equalsIgnoreCase("prime")) {
            return Operations.prime(op);
        } else {
            return Operations.pell(op);
        }
    }

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
}
