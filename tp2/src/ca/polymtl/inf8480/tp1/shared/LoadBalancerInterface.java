package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoadBalancerInterface extends Remote {
    int calculateServerAvailabilty(String filepath) throws RemoteException;
}
