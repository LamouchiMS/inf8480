package ca.polymtl.inf8480.tp2.nameRepository;

import java.rmi.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import ca.polymtl.inf8480.tp2.shared.*;

public class NameRepository implements NameRepositoryInterface {
    private int port;

    public NameRepository(int port) {
        super();
        this.port = port;
    }

    public static void main(String[] args) {
        NameRepository nameRepository = new NameRepository(Integer.parseInt(args[0]));
        nameRepository.run();
    }

    private void run() {
        StubManager.registerNameRepositoryStub(this, port);
    }

    @Override
    public boolean authenticateLoadBalancer(String username, String password) throws RemoteException {
        Config configuration = new Config();
        return username.equals(configuration.getLoadBalancerUsername())
                && password.equals(configuration.getLoadBalancerPassword());
    }

    @Override
    public ArrayList<String> getServerList() throws RemoteException {
        System.out.println("[*]\tGetting the list of servers");
        String rawServers = FileManager.readFile("serversIpList.txt");
        String[] lines = rawServers.split(System.lineSeparator());
        ArrayList<String> cleanLines = new ArrayList<>();
        int MIN_IP_LENGTH = 7;
        
        for (String l : lines) {
            if (l.length() > MIN_IP_LENGTH) {
                cleanLines.add(l);
            }
        }
        System.out.println("[+]\tDone");
        return cleanLines;
    }
}
