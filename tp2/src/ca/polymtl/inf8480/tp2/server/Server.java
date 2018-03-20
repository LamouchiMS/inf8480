package ca.polymtl.inf8480.tp2.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


import ca.polymtl.inf8480.tp2.shared.ServerInterface;
import ca.polymtl.inf8480.tp2.shared.NameRepositoryInterface;
import ca.polymtl.inf8480.tp2.shared.Operations;
import ca.polymtl.inf8480.tp2.shared.Config;;


public class Server implements ServerInterface {
    final int MAX_OPERATIONS = 1000;
    final int MIN_OPERATIONS = 100;
    final int MODULO = 4000;
    private int m = 0; // malice
    private int q = 0; // nbr d'operations acceptable
    private int port = 0;
    private String ipAddress = "";
    private NameRepositoryInterface nameRepositoryStub;

    public Server(int capacity, int malicePercentage, int port) {
        super();

        q = capacity;
        m = malicePercentage;
        this.port = port; 

        Config configuration = new Config();
        String nameRepositoryIP = configuration.getNameRepositoryIP();
        nameRepositoryStub = loadNameRepositoryStub(nameRepositoryIP);
    }

    private NameRepositoryInterface loadNameRepositoryStub(String hostname) {
        NameRepositoryInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (NameRepositoryInterface) registry.lookup("nameRepository");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    public static void main(String[] args) {
        Server server = null;
        if (args.length == 3) {
            server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            server.run();
        } else {
            System.out.println("Format:\tbash ./server <malice> <capacite> <port>");
        }
    }

    public void run() {
        if (System.getSecurityManager() == null) {
            BufferedWriter bw = null;
            FileWriter fw = null;

            try {
                System.setSecurityManager(new SecurityManager());
                ipAddress = InetAddress.getLocalHost().getHostAddress();
                System.setProperty("java.rmi.sever.hostname", ipAddress);

                String content = readFile("nameRepo.txt");
                String[] lines = content.split(System.lineSeparator());
                boolean found = false;
                for (String line : lines) {
                    if (line.trim().equals(ipAddress.trim())) {
                        found = true;
                    }
                }

                if (!found) {
                    fw = new FileWriter("nameRepo.txt", true);
                    bw = new BufferedWriter(fw);
                    bw.write(ipAddress + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null || fw != null)
                        bw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry(port);
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

    // stub to communicate with the load balancer
    private NameRepositoryInterface NameRepositoryInterfaceStub(String hostname) {
        NameRepositoryInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            stub = (NameRepositoryInterface) registry.lookup("nameRepository");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    public boolean isAvailable(int u) {
        int refusingRate = 100 * (u - q) / (5 * q);
        int threshold = (int)Math.random() * 100;
        return threshold < refusingRate;
    }

    public int getQ() {
        return q;
    }

    @Override
    public int calculateSum(String rawOperations) throws RemoteException{

        String[] lines = rawOperations.split(System.lineSeparator());
        int sum = 0;

        for (int i = 0; i < lines.length; i++) {
            String[] parts = lines[i].split(" ");
            sum = (sum + this.calculateLine(parts[0], parts[1])) % MODULO;
        }

        int randomVal = (int) Math.random() * 100;

        if (randomVal < this.m) {
            // Resultat malicieux
            int randomResult = (int) Math.round(Math.random() * Integer.MAX_VALUE);
            return randomResult;
        } else {
            // Resultat correct
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

    public boolean loadBalancerIsAuthenticated(String username, String password)throws RemoteException
    {
        return nameRepositoryStub.authenticateLoadBalancer(username, password);
    }
}
