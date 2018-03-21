package ca.polymtl.inf8480.tp2.shared;

import java.rmi.RemoteException;

public class MyThread implements Runnable {
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
        // On verifie si le repartiteur est authentifie par le seveur avant de commencer les operations
        try {
            if (serverStub.loadBalancerIsAuthenticated(this.usernameLb, this.passwordLb)) {
                result = serverStub.calculateSum(this.rawOperations);
            } else {
                System.out.println("Load balancer not authenticated");
                result = 0;
            }
        }  catch (Exception ex) {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, new Exception());
        }
    }

    public int getResult() {
        return this.result;
    }
}
