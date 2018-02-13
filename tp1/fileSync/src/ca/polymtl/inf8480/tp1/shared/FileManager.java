package ca.polymtl.inf8480.tp1.shared;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.math.*;

public class FileManager {
    private LockManager lm = new LockManager();

    public boolean isSameChecksum(String fileName, String checksum) {
        return getChecksum(fileName).equals(checksum);
    }

    public MyFile lock(String fileName, String clientID, String checksum) {
        MyFile result = null;
        if (lm.tryLock(fileName, clientID)) {
            String content = isSameChecksum(fileName, checksum) ? null : readFile(fileName);
            result = new MyFile(fileName, clientID, content);
        }

        return result;
    }

    public static String getChecksum(String fileName) {
        String checksum = null;
        try {
            String content = readFile(fileName);
            byte[] bytesOfMessage = content.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytesOfMessage);
            BigInteger bigInt = new BigInteger(1, digest);
            checksum = bigInt.toString(16);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return checksum;
    }

    public boolean writeFile(String fileName, String content, String clientID) {
        if (lm.getLockClientID(fileName).equalsIgnoreCase(clientID)) {
            try {
                FileWriter fw = new FileWriter(fileName, false);
                fw.write(content);
                lm.unlock(fileName);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
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

    public boolean createFile(String fileName) {
        boolean isFileCreated = false;
        try {
            File file = new File(fileName);
            isFileCreated = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isFileCreated;
    }

    public ArrayList<MyFile> listFiles() {
        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();
        ArrayList<MyFile> files = new ArrayList<MyFile>();

        for (File f : listOfFiles) {
            if (f.isFile()) {
                String fileName = f.getName();
                String lockClientID = this.lm.getLockClientID(fileName);

                files.add(new MyFile(fileName, lockClientID));
            }
        }

        return files;
    }
}