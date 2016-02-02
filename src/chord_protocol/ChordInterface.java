import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

/**
 * Created by Charandeep on 4/21/15.
 * 
 * CSci5105 Spring 2015
 * Assignment# 7
 * name: <Ravali Kandur>, <Charandeep Parisineti>
 * student id: <5084769>, <5103173>
 * x500 id: <kandu009>, <paris102>
 * CSELABS machine: In README
 * 
 */
/**
 * Interface which represents a node in the CHORD ring 
 * 
 * @author rkandur
 * 
 * CSci5105 Spring 2015
 * Assignment# 7
 * name: <Ravali Kandur>, <Charandeep Parisineti>
 * student id: <5084769>, <5103173>
 * x500 id: <kandu009>, <paris102>
 * CSELABS machine: In README
 *
 */
public interface ChordInterface extends Remote{

    public NodeInfo getMyInfo() throws RemoteException;
   
    public JoinResponse join(String url) throws RemoteException;
    public void join_done(NodeInfo newNode) throws RemoteException;
    public FindNodeResponsePair find_node(String key, boolean withTrace) throws RemoteException;
    
    public String lookup(String word) throws RemoteException;
    public NodeInfo getThisPredecessor() throws RemoteException;
    public NodeInfo getThisSuccessor() throws RemoteException;
    public NodeInfo successor(BigInteger key, StringBuilder logTrace) throws RemoteException;
    
    public void insertKey(String word, String meaning) throws RemoteException;
    public void removeKey(String word) throws RemoteException;
    public void updateSuccessor(NodeInfo successor) throws RemoteException;
    public void notify(NodeInfo predecessor) throws RemoteException;
    
    public void fixFingers() throws RemoteException;
    public Hashtable<String, String> getKeyStore()  throws RemoteException;
    public String printRingStructure() throws RemoteException;
	public String getFormattedNodeDetails(boolean withFingers) throws RemoteException;
 
	public String closestPrecedingNode(BigInteger key, StringBuilder logTrace) throws RemoteException;
	public String printRingStructureWithoutFingers() throws RemoteException;
	
}