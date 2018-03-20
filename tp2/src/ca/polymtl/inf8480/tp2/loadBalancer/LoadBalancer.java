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
import ca.polymtl.inf8480.tp2.shared.StubManager;
import ca.polymtl.inf8480.tp2.shared.FileManager;

public class LoadBalancer implements LoadBalancerInterface {
    private String username;
    private String password;
    private String operationsFile;
    private NameRepositoryInterface nameRepositoryStub;
    private ServerInterface serverStub;

    public LoadBalancer(String username, String password, String operationsFilePath) {
        // super();

        // if (System.getSecurityManager() == null) {
        //     System.setSecurityManager(new SecurityManager());
        // }
        this.operationsFile = operationsFilePath;
        this.username = username;
        this.password = password;

        Config configuration = new Config();
        String nameRepositoryIP = configuration.getNameRepositoryIP();
        nameRepositoryStub = StubManager.loadNameRepositoryStub(nameRepositoryIP);
    }

    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer(args[0], args[1], args[2]);
        loadBalancer.run();
    }

    @Override
    public int calculate(String filePath, String username, String password) throws RemoteException {
        long startTime = System.nanoTime();
      
        // Get servers
        ArrayList<String> serverList = nameRepositoryStub.getServerList();
        ArrayList<ServerInterface> serverStubs = new ArrayList<>();
        for (String serverIP : serverList) {
            serverStub = StubManager.loadServerStub(serverIP.trim());
            serverStubs.add(serverStub);
        }

        // Operations
        String rawOperations = FileManager.readFile(filePath);
        String[] operations = rawOperations.split(System.lineSeparator());
        
        // Threads
        ArrayList<MyThread> threads = new ArrayList<>();
        int result = -1;

        Config c = new Config();
        if (c.isLoadBalancerSecure()) {
            // Secure mode
            int idx = 0;
            boolean lastOperation = false;

            while ((idx < operations.length) && !lastOperation) {
                for (ServerInterface computingServer : serverStubs) {
                    if (computingServer != null) {
                        int blockSize = computingServer.getQ();

                        if ((idx + computingServer.getQ()) >= operations.length) {
                            blockSize = operations.length - idx;
                            lastOperation = true;
                        }
                        String rawBlock = "";
                
                        for (int pointer = 0; pointer < blockSize; pointer++) {
                            rawBlock += operations[pointer + idx] + '\n';
                        }
                        
                        idx += blockSize;

                        // Start thread
                        MyThread t = new MyThread(username, password, computingServer, rawBlock);
                        t.start();
                        threads.add(t);
                        
                        if (lastOperation) break;
                    }
                }
            }

            // After all threads are done
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
            // Mode non securise
        }

        return result;
    }

    private void run() {
        StubManager.registerLoadBalancerStub(this);
        // authenticate and calculate the operations of the file
        try {
            calculate(operationsFile, username, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
