package test;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class BankServerImpl extends UnicastRemoteObject implements
		RMIBankServer {

	private static final long serialVersionUID = 1L;
	
	protected BankServerImpl() throws RemoteException {
		super();
	}

	public void execute() {
		System.out.println("In execute of BankServerImpl");
	}

	public static void main(String[] args) {

		System.setSecurityManager(new RMISecurityManager());
		BankServerImpl impl;

		try {
			impl = new BankServerImpl();
			Naming.bind("RMIBankServer", impl);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
