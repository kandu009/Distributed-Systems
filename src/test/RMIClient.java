package test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

public class RMIClient {

	public static void main(String[] args) {

		if (args.length != 1) {
			throw new RuntimeException("RMICLient <host>");
		}
		System.setSecurityManager(new RMISecurityManager());
		try {
			RMIBankServer impl = (RMIBankServer) Naming.lookup("//" + args[0]+ "/RMIBankServer");
			impl.execute();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}