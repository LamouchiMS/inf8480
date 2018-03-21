package ca.polymtl.inf8480.tp2.server;

import java.rmi.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import ca.polymtl.inf8480.tp2.shared.*;

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
        int nameRepositoryPort = configuration.getNameRepositoryPort();        
        nameRepositoryStub = StubManager.loadNameRepositoryStub(nameRepositoryIP, nameRepositoryPort);
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

    private boolean isIpAlreadyWritten(String ipAddress) {
        String content = FileManager.readFile("serversIpList.txt");
        String[] lines = content.split(System.lineSeparator());
        for (String line : lines) {
            if (line.trim().equals(ipAddress.trim())) {
                return true;
            }
        }
        return false;
    }

    private void registerHostInFile() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
            System.setProperty("java.rmi.sever.hostname", ipAddress);
            System.setProperty("java.rmi.activation.port", port + "");
            if (!this.isIpAlreadyWritten(ipAddress+":"+port)) {
                FileManager.appendToFile("serversIpList.txt", ipAddress+":"+port);
            }
        } catch (java.net.UnknownHostException e) {
            System.err.println("Error while registering server");
            e.printStackTrace();
        }
    }

    public void run() {
        this.registerHostInFile();
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
        // if (port == 5003) {
        //     System.exit(0);
        // }
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
