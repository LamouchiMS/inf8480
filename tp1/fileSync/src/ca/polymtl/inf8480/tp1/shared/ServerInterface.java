package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
    String createClientID() throws RemoteException;

    boolean create(String name) throws RemoteException;

    ArrayList<MyFile> list() throws RemoteException;

    MyFile lock(String name, String clientID, String checksum) throws RemoteException;

    ArrayList<MyFile> syncLocalDirectory() throws RemoteException;

    String get(String name, String checksum) throws RemoteException;

    boolean push(String name, String content, String clientID) throws RemoteException;
}
