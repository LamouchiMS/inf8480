package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import ca.polymtl.inf8480.tp1.shared.ClientManager;
import ca.polymtl.inf8480.tp1.shared.FileManager;
import ca.polymtl.inf8480.tp1.shared.MyFile;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Server implements ServerInterface {
    private final static ClientManager clientManager = new ClientManager();
    private final static FileManager fileManager = new FileManager();

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    public Server() {
        super();
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("server", stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public String createClientID() throws RemoteException {
        return clientManager.createClientID();
    }

    @Override
    public boolean create(String name) throws RemoteException {
        return fileManager.createFile(name);
    }

    @Override
    public ArrayList<MyFile> list() throws RemoteException {
        return fileManager.listFiles();
    }

    @Override
    public MyFile lock(String name, String clientID, String checksum) throws RemoteException {
        return fileManager.lock(name, clientID, checksum);
    }

    @Override
    public ArrayList<MyFile> syncLocalDirectory() throws RemoteException {
        ArrayList<MyFile> files = fileManager.listFiles();
        for (MyFile f : files) {
            f.setContent(fileManager.readFile(f.getContent()));
        }
        return files;
    }

    @Override
    public String get(String name, String checksum) throws RemoteException {
        if (fileManager.isSameChecksum(name, checksum)) {
            return null;
        } else {
            return fileManager.readFile(name);
        }
    }

    @Override
    public boolean push(String name, String content, String clientID) throws RemoteException {
        return false;
    }
}
