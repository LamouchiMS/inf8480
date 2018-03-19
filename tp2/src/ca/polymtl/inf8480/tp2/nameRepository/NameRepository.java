package ca.polymtl.inf8480.tp2.nameRepository;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.rmi.registry.Registry;

import java.util.Random;
import java.util.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

import ca.polymtl.inf8480.tp2.shared.NameRepositoryInterface;
import ca.polymtl.inf8480.tp2.shared.Config;

public class NameRepository implements NameRepositoryInterface {

    public NameRepository() {
        super();
    }

    public static void main(String[] args) {
        NameRepository nameRepository = new NameRepository();
        nameRepository.run();
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            NameRepositoryInterface stub = (NameRepositoryInterface) UnicastRemoteObject.exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("nameRepository", stub);
            System.out.println("Name repository ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public boolean authenticateLoadBalancer(String username, String password) throws RemoteException {
        Config configuration = new Config();
        return username.equals(configuration.getLoadBalancerUsername())
                && password.equals(configuration.getLoadBalancerPassword());
    }

    public String readFile(String fileName) {
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

    @Override
    public String[] getServerList() throws RemoteException {

        String rawServers = this.readFile("nameRepo.txt");
        String[] lines = rawServers.split(System.lineSeparator());
        ArrayList<String> cleanLines = new ArrayList<>();
        int MIN_IP_LENGTH = 7;
        
        for (String l : lines) {
            if (l.length() > MIN_IP_LENGTH) {
                cleanLines.add(l);
            }
        }
        return (String[]) cleanLines.toArray();
    }
}
