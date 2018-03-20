package ca.polymtl.inf8480.tp2.shared;

import java.rmi.RemoteException;

public class MyThread extends Thread {
    public ServerInterface serverStub;
    public int result = 0;
    public String rawOperations;
    private String usernameLb;
    private String passwordLb;

    public MyThread(String usernameLoadBalancer, String passwordLoadBalancer, ServerInterface serverStub,
            String rawOperations) {
        this.serverStub = serverStub;
        this.rawOperations = rawOperations;
        this.usernameLb = usernameLoadBalancer;
        this.passwordLb = passwordLoadBalancer;

        run();
    }

    @Override
    public void run() {
        try {
            // On verifie si le repartiteur est authentifie par le seveur avant de commencer les operations
            if (serverStub.loadBalancerIsAuthenticated(this.usernameLb, this.passwordLb)) {
                result = serverStub.calculateSum(this.rawOperations);
            } else {
                System.out.println("Load balancer not authenticated");
                result = 0;
            }
        } catch (RemoteException e) {
            System.out.println("Error while computing thread : " + e.getMessage());
        }
    }

    public int getResult() {
        return this.result;
    }
}
