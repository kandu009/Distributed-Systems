package test;

import java.net.UnknownHostException;

public class TestMain {

	public static void main(String[] args) {

		try {
			String s = java.net.InetAddress.getLocalHost().getHostName();
			System.out.println(s);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

}
