package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RepositoryInterface extends Remote {
    String[] getServerList() throws RemoteException;
    boolean authenticateLoadBalancer(String username, String password, String ip) throws RemoteException;
}
