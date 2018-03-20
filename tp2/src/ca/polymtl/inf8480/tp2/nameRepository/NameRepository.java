package ca.polymtl.inf8480.tp2.nameRepository;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
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
import ca.polymtl.inf8480.tp2.shared.StubManager;
import ca.polymtl.inf8480.tp2.shared.Config;
import ca.polymtl.inf8480.tp2.shared.FileManager;;

public class NameRepository implements NameRepositoryInterface {

    public NameRepository() {
        super();
    }

    public static void main(String[] args) {
        NameRepository nameRepository = new NameRepository();
        nameRepository.run();
    }

    private void run() {
        StubManager.registerNameRepositoryStub(this);
    }

    @Override
    public boolean authenticateLoadBalancer(String username, String password) throws RemoteException {
        Config configuration = new Config();
        return username.equals(configuration.getLoadBalancerUsername())
                && password.equals(configuration.getLoadBalancerPassword());
    }

    @Override
    public ArrayList<String> getServerList() throws RemoteException {

        System.out.println("getting the server list");
        String rawServers = FileManager.readFile("nameRepo.txt");
        String[] lines = rawServers.split(System.lineSeparator());
        ArrayList<String> cleanLines = new ArrayList<>();
        int MIN_IP_LENGTH = 7;

        for (String l : lines) {
            if (l.length() > MIN_IP_LENGTH) {
                cleanLines.add(l);
                System.out.println(l);

            }
        }
        return cleanLines;
    }
}
