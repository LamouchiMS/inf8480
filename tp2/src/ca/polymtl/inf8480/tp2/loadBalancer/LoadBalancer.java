package ca.polymtl.inf8480.tp2.loadBalancer;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.Random;
import java.util.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

import ca.polymtl.inf8480.tp2.shared.LoadBalancerInterface;
import ca.polymtl.inf8480.tp2.shared.MyThread;
import ca.polymtl.inf8480.tp2.shared.NameRepositoryInterface;
import ca.polymtl.inf8480.tp2.shared.ServerInterface;
import ca.polymtl.inf8480.tp2.shared.Config;

public class LoadBalancer implements LoadBalancerInterface {
    private String username;
    private String password;
    private NameRepositoryInterface nameRepositoryStub;
    private ServerInterface serverStub;

    public LoadBalancer() {
        super();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        Config configuration = new Config();
        String nameRepositoryIP = configuration.getNameRepositoryIP();
        nameRepositoryStub = loadNameRepositoryStub(nameRepositoryIP);
    }

    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.run();
    }

    @Override
    public int calculate(String filePath, String username, String password) throws RemoteException {
        long startTime = System.nanoTime();

        // Get servers
        String[] serverList = nameRepositoryStub.getServerList();
        System.out.println("after getting the server list...");
        System.out.println(serverList);

        ArrayList<ServerInterface> serverStubs = new ArrayList<>();
        for (String serverIP : serverList) {
            serverStubs.add(loadServerStub(serverIP.trim()));
        }

        // Operations
        String rawOperations = readFile(filePath);
        String[] operations = rawOperations.split(System.lineSeparator());

        // Threads
        ArrayList<MyThread> threads = new ArrayList<>();
        int result = -1;

        Config c = new Config();

        System.out.println("ici 3...");
        if (c.isLoadBalancerSecure()) {
            // Secure mode

            int idx = 0;
            boolean lastOperation = false;

            while ((idx < operations.length) && !lastOperation) {
                for (ServerInterface computingServer : serverStubs) {
                    if (computingServer != null) {
                        int blockSize = computingServer.getQ();
                        if (idx + computingServer.getQ() >= operations.length) {
                            blockSize = operations.length - idx;
                            lastOperation = true;
                        }
                        String rawBlock = "";

                        for (int pointer = 0; pointer < blockSize; pointer++) {
                            rawBlock.concat(operations[pointer + idx]).concat(System.lineSeparator());
                        }

                        idx += blockSize;

                        MyThread t = new MyThread(computingServer, rawBlock);
                        t.start();
                        threads.add(t);
                    }
                }
            }

            // After all threads are done
            System.out.println("ici 4...");
            for (MyThread t : threads) {
                try {
                    t.join();
                    result += t.getResult();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            long endTime = System.nanoTime(); //afficher le temps d'execution
            float tempsExecution = (float) (endTime - startTime) / 1000000;
            System.out.println("\nTemps d'exécution (ms) = " + Float.toString(tempsExecution) + " milliSecondes");
            System.out.println("\nNombre d'opérations = " + operations.length + "\n");

        } else {

        }

        return result;
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            LoadBalancerInterface stub = (LoadBalancerInterface) UnicastRemoteObject.exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("loadBalancer", stub);
            System.out.println("Load balancer ready.");

            System.out.println("Calculating operations...");
            calculate("operations-588", "inf8480", "1234" );
           

        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
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

}
