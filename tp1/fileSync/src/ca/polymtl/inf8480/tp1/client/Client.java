package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.io.*;

import ca.polymtl.inf8480.tp1.shared.FileManager;
import ca.polymtl.inf8480.tp1.shared.MyFile;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {
    String realIP = "132.207.12.220";
    private static final String REMOTE_IP = "127.0.0.1";
    private final static String[] COMMANDS = { "createClientID", "create", "list", "syncLocalDirectory", "get", "lock",
            "push" };
    private ServerInterface distantServerStub = null;
    private final static String CLIENT_ID_FILE = "clientId.txt";

    public static void main(String[] args) {
        if (args.length > 0) {
            String command = args[0];
            if (isValidCommand(command)) {
                Client client = new Client(REMOTE_IP);

                if (command.equalsIgnoreCase("list")) {
                    client.listFiles();
                } else if (command.equalsIgnoreCase("create")) {
                    if (args.length == 2) {
                        client.create(args[1]);
                    } else {
                        System.err.println("File name missing");
                    }
                } else if (command.equalsIgnoreCase("createClientID")) {
                    client.createClientID();
                } else if (command.equalsIgnoreCase("get")) {
                    client.get(args[1]);
                } else if (command.equalsIgnoreCase("lock")) {
                    client.lock(args[1]);
                } else if (command.equalsIgnoreCase("push")) {
                    client.push(args[1]);
                } else if (command.equalsIgnoreCase("syncLocalDirectory")) {
                    client.sync();
                }
            } else {
                printInvalidCommand();
            }
        } else {
            printInvalidCommand();
        }
    }

    public Client(String distantServerHostname) {
        super();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        distantServerStub = loadServerStub(distantServerHostname);
    }

    public void sync() {
        try {
            ArrayList<MyFile> files = distantServerStub.syncLocalDirectory();
            System.out.println(files);
            try {
                for (int i = 0; i < files.size(); i++) {
                    File f = new File(files.get(i).getName());
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                    FileWriter fw = new FileWriter(files.get(i).getName(), false);
                    String content = files.get(i).getContent();
                    fw.write(content);
                    fw.close();
                }
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void lock(String fileName) {
        try {
            File f = new File(fileName);
            String checksum = null;
            if (f.exists()) {
                checksum = FileManager.getChecksum(fileName);
            } else {
                f.createNewFile();
            }
            String clientID = FileManager.readFile(CLIENT_ID_FILE);
            MyFile file = distantServerStub.lock(fileName, clientID, checksum);

            if (file.getContent() != null) {
                try {
                    FileWriter fw = new FileWriter(fileName, false);
                    fw.write(file.getContent());
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void push(String fileName) {
        try {
            String content = FileManager.readFile(fileName);
            String clientID = FileManager.readFile(CLIENT_ID_FILE);

            distantServerStub.push(fileName, content, clientID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void get(String fileName) {
        try {
            File f = new File(fileName);

            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String checksum = FileManager.getChecksum(fileName);
            String content = distantServerStub.get(fileName, checksum);

            if (content != null) {
                try {
                    FileWriter fw = new FileWriter(fileName, false);
                    fw.write(content);
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void createClientID() {
        try {
            File f = new File(CLIENT_ID_FILE);
            if (!f.exists()) {
                f.createNewFile();
                try {
                    String clientID = distantServerStub.createClientID();

                    FileWriter fw = new FileWriter(CLIENT_ID_FILE, false);
                    fw.write(clientID);
                    fw.close();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void create(String fileName) {
        try {
            distantServerStub.create(fileName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void listFiles() {
        try {
            ArrayList<MyFile> files = distantServerStub.list();
            for (int i = 0; i < files.size(); i++) {
                MyFile f = files.get(i);
                String lockID = f.getLockClientID();
                String name = f.getName();
                String lockInfo = lockID == null ? "non verouille" : "verouille par " + lockID;
                System.out.println("* " + name + "\t" + lockInfo);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Print an error message when the command is executed with invalid parameters
    private static void printInvalidCommand() {
        System.out.println("Type one of the following commands");
        for (String cmd : COMMANDS) {
            System.out.println("\t" + cmd);
        }
    }

    // Returns true if the command is called with valid paramters
    private static boolean isValidCommand(String cmd) {
        boolean isValidCommand = false;
        for (int i = 0; i < COMMANDS.length && !isValidCommand; i++) {
            if (COMMANDS[i].equalsIgnoreCase(cmd)) {
                isValidCommand = true;
            }
        }
        return isValidCommand;
    }

    private ServerInterface loadServerStub(String hostname) {
        ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }
}
