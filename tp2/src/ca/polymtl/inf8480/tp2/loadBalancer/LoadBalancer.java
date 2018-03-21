package ca.polymtl.inf8480.tp2.loadBalancer;

import java.rmi.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import java.util.concurrent.*;
import ca.polymtl.inf8480.tp2.shared.*;

public class LoadBalancer implements LoadBalancerInterface {
    private String username;
    private String password;
    private String operationsFile;
    private NameRepositoryInterface nameRepositoryStub;
    private ServerInterface serverStub;
    private int port;

    public LoadBalancer(String username, String password, String operationsFilePath, int port) {
        this.operationsFile = "./operations/" + operationsFilePath;
        this.username = username;
        this.password = password;
        this.port = port;

        Config configuration = new Config();
        String nameRepositoryIP = configuration.getNameRepositoryIP();
        int nameRepositoryPort = configuration.getNameRepositoryPort();
        nameRepositoryStub = StubManager.loadNameRepositoryStub(nameRepositoryIP, nameRepositoryPort);
    }

    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer(args[0], args[1], args[2], Integer.parseInt(args[3]));
        loadBalancer.run();
    }

    private int getOperationsLength(String filePath) {
        String rawOperations = FileManager.readFile(filePath);
        String[] operations = rawOperations.split(System.lineSeparator());
        return operations.length;   
    }

    private ArrayList<ServerInterface> getServerStubs(ArrayList<String> serverList) {
        ArrayList<ServerInterface> serverStubs = new ArrayList<>();
        for (String serverConfig : serverList) {
            String[] parts = serverConfig.trim().split(":");
            serverStub = StubManager.loadServerStub(parts[0], Integer.parseInt(parts[1]));
            serverStubs.add(serverStub);
        }
        return serverStubs;
    }

    private Callable<Integer> createCallableThread(ServerInterface serverStub, String rawOperations) {
        return new Callable<Integer> () {
            public Integer call() throws Exception {
                int r = -1;
                try {
                    if (serverStub.loadBalancerIsAuthenticated(username, password)) {
                        r = serverStub.calculateSum(rawOperations) % 4000;
                    } else {
                        System.out.println("Load balancer not authenticated");
                    }
                } catch(Exception e) {
                    System.out.println("\n[!]\tDead stub detected");
                } finally {
                    return r;
                }
            }
        };
    }

    @Override
    public int calculate(String filePath, String username, String password) throws RemoteException {
        // Get servers
        ArrayList<String> serverList = nameRepositoryStub.getServerList();        
        ArrayList<ServerInterface> serverStubs = this.getServerStubs(serverList);

        // Operations
        String[] operations = FileManager.readFile(filePath).split(System.lineSeparator());
        
        // Threads
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future> futures = new ArrayList<Future> ();
        int result = -1;

        Config c = new Config();
        if (c.isLoadBalancerSecure()) {
            // Secure mode
            int idx = 0;
            boolean lastOperation = false;

            while ((idx < operations.length) && !lastOperation && serverStubs.size() > 0) {
                for (int i = 0; i < serverStubs.size() && !lastOperation; i++) {
                    ServerInterface computingServer = serverStubs.get(i);
                    if (computingServer != null) {
                        int blockSize = computingServer.getQ();

                        if ((idx + blockSize) >= operations.length) {
                            blockSize = operations.length - idx;
                            lastOperation = true;
                        }
                        String rawBlock = "";
                        for (int pointer = 0; pointer < blockSize; pointer++) {
                            rawBlock += operations[pointer + idx] + '\n';
                        }
                        final String rawData = rawBlock;
                        idx += blockSize;

                        Callable<Integer> callable = this.createCallableThread(computingServer, rawData);
                        futures.add(executor.submit(callable));
                    } else {                         
                        System.out.println("Stub is down at index = "+i+" => skip & re-distribute workload");
                    }
                }

                // Wait for threads before next interation
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        int tmpRes = (int) futures.get(i).get();
                        if(tmpRes == -1) {
                            System.out.println("[+]\tDead stub is at "+serverList.get(i)+"\t=> Marked");
                            serverStubs.remove(i);
                            System.out.println("[+]\tStub removed from available list\t=> Re-distribute workload");
                        } else {
                            // Stub is fine
                            result = (result + tmpRes) % 4000;
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    } finally {
                        futures.get(i).cancel(true);
                    }
                }
                // Reload threads
                executor.shutdownNow();
                executor = Executors.newCachedThreadPool();
                futures = new ArrayList<Future> ();
            }
        } else {
            // Mode non securise
        }

        return result;
    }

    private void run() {
        StubManager.registerLoadBalancerStub(this, port);
        // authenticate and calculate the operations of the file
        try {
            System.out.println("[*]\tCalculating...");
            long startTime = System.nanoTime();            
            int result = calculate(operationsFile, username, password);
            long endTime = System.nanoTime();      
            
            float tempsExecution = (float) (endTime - startTime) / 1000000;
            System.out.println();
            System.out.println("Temps d'exécution (ms) = " + Float.toString(tempsExecution) + " milliSecondes");
            System.out.println("Nombre d'opérations    = " + this.getOperationsLength(operationsFile));
            System.out.println("Resultat               = " + result + "\n");      
        } catch (RemoteException e) {
            System.err.println("Error while running load balancer");
            e.printStackTrace();
        }
    }
}
