package ca.polymtl.inf8480.tp2.shared;

import java.rmi.*;

public interface ServerInterface extends Remote {
    int calculateSum(String rawOperations) throws RemoteException;
    boolean loadBalancerIsAuthenticated(String username, String password)throws RemoteException;
    int getQ() throws RemoteException;
}
