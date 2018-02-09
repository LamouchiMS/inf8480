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
                clientID = f.getLockClientID();
            }
        }
        return clientID;
    }

    private void syncLockFile() {
        try {
            FileWriter fw = new FileWriter(LOCK_FILE_NAME, false);
            for (MyFile f : files) {
                fw.write(f.getName() + "\t" + f.getLockClientID() + System.lineSeparator());
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lock(String fileName, String lockClientID) {
        MyFile f = new MyFile(fileName, lockClientID);
        files.add(f);
        syncLockFile();
    }

    public void unlock(String fileName) {
        int idx = -1;
        for (int i = 0; i < files.size() && idx == -1; i++) {
            if (files.get(i).getName().equalsIgnoreCase(fileName)) {
                idx = i;
            }
        }
        files.remove(idx);
        syncLockFile();
    }

    public boolean tryLock(String fileName, String lockClientID) {
        String currentLockID = getLockClientID(fileName);
        if (currentLockID == null || currentLockID.equalsIgnoreCase(lockClientID)) {
            lock(fileName, lockClientID);
            return true;
        } else {
            return false;
        }
    }
}