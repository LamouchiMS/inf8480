package ca.polymtl.inf8480.tp2.shared;

import java.rmi.RemoteException;

public class MyThread extends Thread {

	public ServerInterface serverStub;
	public int result = 0;
	public String rawOperations;
	private String usernameLb;
	private String passwordLb;

	public MyThread(String usernameLB, String passwordLB, ServerInterface serverStub, String rawOperations){
		
		this.serverStub = serverStub;
		this.rawOperations = rawOperations;
		this.usernameLb = usernameLB;
		this.passwordLb = passwordLB;
		
		run();
   	}

	@Override
	public void run() {
		try {

			// on verifie si le repartiteur est authentifie par le seveur avant de commencer les operations
			if (serverStub.loadBalancerIsAuthenticated(this.usernameLb, this.passwordLb))
			{
				result = serverStub.calculateSum(this.rawOperations);
				
			}
		} 
		catch (RemoteException e) // En cas de panne, on copie le reste des operation dans une liste 
		{
			System.out.println("Error while computing thread : " + e.getMessage());
		}
	}

	public int getResult() {
		return this.result;
	}
}
