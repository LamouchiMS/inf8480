package ca.polymtl.inf8480.tp2.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Random;
import java.util.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

import ca.polymtl.inf8480.tp2.shared.ServerInterface;
import ca.polymtl.inf8480.tp2.shared.StubManager;
import ca.polymtl.inf8480.tp2.shared.NameRepositoryInterface;
import ca.polymtl.inf8480.tp2.shared.Operations;
import ca.polymtl.inf8480.tp2.shared.Config;
import ca.polymtl.inf8480.tp2.shared.FileManager;

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
        nameRepositoryStub = StubManager.loadNameRepositoryStub(nameRepositoryIP);
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

    private boolean isIpAlreadyWritten() {
        String content = FileManager.readFile("nameRepo.txt");
        String[] lines = content.split(System.lineSeparator());
        for (String line : lines) {
            if (line.trim().equals(this.ipAddress.trim())) {
                return true;
            }
        }
        return false;
    }

    private void registerIPinFile() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
            System.setProperty("java.rmi.sever.hostname", ipAddress);
            FileManager.appendToFile("nameRepo.txt", ipAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (!this.isIpAlreadyWritten())
            this.registerIPinFile();
        StubManager.registerServerStub(this, port);
    }

    public boolean isAvailable(int u) {
        int refusingRate = 100 * (u - q) / (5 * q);
        int threshold = (int) Math.random() * 100;
        return threshold < refusingRate;
    }

    public int getQ() {
        return q;
    }

    @Override
    public int calculateSum(String rawOperations) throws RemoteException {
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

    public boolean loadBalancerIsAuthenticated(String username, String password) throws RemoteException {
        return nameRepositoryStub.authenticateLoadBalancer(username, password);
    }
}
