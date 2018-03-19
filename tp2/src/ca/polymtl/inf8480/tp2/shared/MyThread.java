package ca.polymtl.inf8480.tp2.shared;

import java.rmi.RemoteException;

public class MyThread extends Thread {

	public ServerInterface serverStub;
	public int result;
	public String rawOperations;

	public MyThread(ServerInterface serverStub, String rawOperations){
    	this.serverStub = serverStub;
    	this.rawOperations = rawOperations;
   		
    	run();
   	}

	@Override
	public void run() {
		try {
			result = serverStub.calculateSum(this.rawOperations);
		} catch (RemoteException e) // En cas de panne, on copie le reste des operation dans une liste 
		{
			System.out.println("Error while computing thread : " + e.getMessage());
		}
	}

	public int getResult() {
		return this.result;
	}
}
