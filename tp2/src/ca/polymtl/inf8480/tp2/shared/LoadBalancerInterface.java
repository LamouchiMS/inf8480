package ca.polymtl.inf8480.tp2.shared;

import java.rmi.*;

public interface LoadBalancerInterface extends Remote {
    public int calculate(String filePath, String username, String password) throws RemoteException;
}
