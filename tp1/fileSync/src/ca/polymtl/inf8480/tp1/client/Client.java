package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {
    private static final String REMOTE_IP = "127.0.0.1";
    private final static String[] COMMANDS = { "createClientID", "create", "list", "syncLocalDirectory", "get", "lock",
            "push" };
    private ServerInterface distantServerStub = null;

    public static void main(String[] args) {
        // String distantHostname = null;
        String command = args[0];

        if (args.length > 0 && isValidCommand(command)) {
            Client client = new Client(REMOTE_IP);
            // client.execute(command);

        } else {
            printInvalidCommand();
        }

        // client.run();
    }

    public Client(String distantServerHostname) {
        super();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        distantServerStub = loadServerStub(distantServerHostname);
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

    private void execute() {
        File folder = new File("your/path");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }

    private void run() {
        try {
            // MyFile[] result = distantServerStub.list();
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
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
