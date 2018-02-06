package ca.polymtl.inf8480.tp1.shared;

import java.io.*;
import java.util.ArrayList;

public class LockManager {
    private ArrayList<MyFile> files = new ArrayList<MyFile>();
    private final static String LOCK_FILE_NAME = "lockMetadata.txt";

    public LockManager() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(LOCK_FILE_NAME));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String content = sb.toString();

            String[] contentLines = content.split(System.lineSeparator());
            for (String contentLine : contentLines) {
                String[] parts = contentLine.split("\t");
                String name = parts[0];
                String lockClientID = parts[1];
                MyFile f = new MyFile(name, lockClientID);

                this.files.add(f);
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLockClientID(String fileName) {
        String clientID = null;
        for (MyFile f : files) {
            if (f.getName().equalsIgnoreCase(fileName)) {
                clientID = f.getName();
            }
        }
        return clientID;
    }

    public boolean lock(String fileName, String lockClientID) {
        try {
            FileWriter fw = new FileWriter(LOCK_FILE_NAME, true);
            fw.write(fileName + "\t" + lockClientID + System.lineSeparator());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}