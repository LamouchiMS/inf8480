package ca.polymtl.inf8480.tp2.nameRepository;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;

import ca.polymtl.inf8480.tp2.shared.NameRepositoryInterface;
import ca.polymtl.inf8480.tp2.shared.Config;

public class NameRepository implements NameRepositoryInterface {
    public static void main(String[] args) {
        NameRepository nameRepository = new NameRepository();
        nameRepository.run();
    }

    public NameRepository() {
        super();
    }

    private void run() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            NameRepositoryInterface stub = (NameRepositoryInterface) UnicastRemoteObject.exportObject(this, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("nameRepository", stub);
            System.out.println("Name repository ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public boolean authenticateLoadBalancer(String username, String password) throws RemoteException {
        Config configuration = new Config();
        return username.equals(configuration.getLoadBalancerUsername())
                && password.equals(configuration.getLoadBalancerPassword());
    }

    @Override
    public String[] getServerList() throws RemoteException {
        return null;
    }
}
