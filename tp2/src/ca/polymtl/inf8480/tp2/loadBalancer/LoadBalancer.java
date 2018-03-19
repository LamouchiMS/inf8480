package ca.polymtl.inf8480.tp2.loadBalancer;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;

import ca.polymtl.inf8480.tp2.shared.LoadBalancerInterface;
import ca.polymtl.inf8480.tp2.shared.NameRepositoryInterface;
import ca.polymtl.inf8480.tp2.shared.Config;

public class LoadBalancer implements LoadBalancerInterface {
    private String username;
    private String password;
    private NameRepositoryInterface nameRepositoryStub;

    public LoadBalancer() {
        super();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        Config configuration = new Config();
        String nameRepositoryIP = configuration.getNameRepositoryIP();
        nameRepositoryStub = loadServerStub(nameRepositoryIP);
    }

    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.run();
    }

    @Override
    public int calculate(String filePath) throws RemoteException {
        
        return 0;
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            LoadBalancerInterface stub = (LoadBalancerInterface) UnicastRemoteObject.exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("loadBalancer", stub);
            System.out.println("Load balancer ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private NameRepositoryInterface loadServerStub(String hostname) {
        NameRepositoryInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
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
}
