package test;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIBankServer extends Remote {
	public void execute() throws RemoteException;
}
