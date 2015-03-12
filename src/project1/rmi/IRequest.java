package project1.rmi;
/**
 * Interface which denotes a request sent by the client to an RMI server
 * 
 * @author rkandur
 *
 */
public interface IRequest {
	
	public Object execute();
	
}
