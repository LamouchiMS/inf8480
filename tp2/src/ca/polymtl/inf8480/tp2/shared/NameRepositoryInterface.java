package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NameRepositoryInterface extends Remote {
    boolean authenticateLoadBalancer(String username, String password) throws RemoteException;
    ArrayList<String> getServerList() throws RemoteException;
}
