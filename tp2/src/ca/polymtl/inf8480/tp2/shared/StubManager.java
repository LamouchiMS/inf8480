package ca.polymtl.inf8480.tp2.shared;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf8480.tp2.nameRepository.NameRepository;
import ca.polymtl.inf8480.tp2.server.Server;
import ca.polymtl.inf8480.tp2.loadBalancer.LoadBalancer;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class StubManager {
    public StubManager() {
    }

    public static NameRepositoryInterface loadNameRepositoryStub(String hostname, int port) {
        NameRepositoryInterface stub = null;
        try {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            stub = (NameRepositoryInterface) registry.lookup("nameRepository");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    public static ServerInterface loadServerStub(String hostname, int port) {
        ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            stub = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        }

        return stub;
    }

    public static void registerNameRepositoryStub(NameRepository instance, int port) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            NameRepositoryInterface stub = (NameRepositoryInterface) UnicastRemoteObject.exportObject(instance, 0);

            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("nameRepository", stub);
            System.out.println("[+]\tName repository ready");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    public static void registerLoadBalancerStub(LoadBalancer instance, int port) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            LoadBalancerInterface stub = (LoadBalancerInterface) UnicastRemoteObject.exportObject(instance, 0);

            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("loadBalancer", stub);
            System.out.println("[+]\tLoad balancer ready");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    public static void registerServerStub(Server instance, int port) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(instance, 0);
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("server", stub);
            System.out.println("[+]\tServer ready");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}