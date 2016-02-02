
/**
 * Created by Charandeep on 4/21/15.
 * 
 * class which denotes a specific node on the Chord ring
 * 
 */

import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
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
public class Node extends UnicastRemoteObject implements ChordInterface{

	private static final long serialVersionUID = 1L;

	private static String MASTER_NODE_URL;
    private NodeInfo myInfo_;
    private NodeInfo mySuccessor_ = null;
    private NodeInfo myPredecessor_ = null;
    private Integer globalNodeCount_ = 0;

    private Hashtable<String,String> dictionary_;
    private String[] fingerTable_;
    private boolean joinLock_ = true;
    
    private ChordInterface masterNode_;
    private static Logger logger;
    private boolean isMasterNode_ = Boolean.FALSE;
    private static int rmiPort_ = 1099;
    
    public Node(String url) throws RemoteException {

    	logger = ServerLogger.logger(url.split("/")[1]);
        dictionary_ = new Hashtable<String,String>();
        fingerTable_ = new String[160];

        try {
        	// just create a new ring with one node in this case
            if (url.equals(MASTER_NODE_URL)) {
            	isMasterNode_ = true;
                myInfo_ = new NodeInfo(url, Utils.sha1BigInt(url), 0);
                myPredecessor_ = this.myInfo_;
                mySuccessor_ = this.myInfo_;
                masterNode_ = this;
				logger.info("Done with initializing Master node. NodeID {"
						+ myInfo_.nodeId_ + "} NodeURL {" + myInfo_.nodeURL_
						+ "}, Successor {" + mySuccessor_.nodeURL_
						+ "}, Predecessor {" + myPredecessor_.nodeURL_ + "}");
            }
            else{
            	// add a new node to the existing ring
            	isMasterNode_ = false;
                Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(MASTER_NODE_URL), rmiPort_);
                masterNode_ = (ChordInterface) registry.lookup(MASTER_NODE_URL);
                JoinResponse joinResponse = masterNode_.join(url);
                if(joinResponse.status == JoinResponse.Status.BUSY){
                    logger.info("Node-0 is busy! Kill and reconnect after sometime.");
                }
                else{
                	myInfo_ = joinResponse.newNodeInfo;
                	Registry pregistry = LocateRegistry.getRegistry(Utils.getHostFromURL(joinResponse.predecessorURL), rmiPort_);
                	Registry sregistry = LocateRegistry.getRegistry(Utils.getHostFromURL(joinResponse.successorURL), rmiPort_);
                    myPredecessor_ = ((ChordInterface) pregistry.lookup(joinResponse.predecessorURL)).getMyInfo();
                    mySuccessor_ = ((ChordInterface) sregistry.lookup(joinResponse.successorURL)).getMyInfo();
                    fingerTable_ = joinResponse.fingerTable;
                    logger.info("New node {"
                    		+ joinResponse.newNodeInfo.nodeURL_
                    		+ "} joined successfully ! Successor {"
                    		+ mySuccessor_.nodeURL_ + "}, Predecessor {"
                    		+ myPredecessor_.nodeURL_ + "}");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            joinLock_ = true;	// this is to make sure that no other servers are blocked if an exception occurs
            throw new RemoteException("Failed to initiate a new node. Please try again Later!");
        }

    }

    /*
     * used to join a new node in the Chord ring
     */
    @Override
    public JoinResponse join(String url) throws RemoteException {
    	
        if(joinLock_==false){
        	logger.info("Master node is Busy and cannot accept any new Join requests now, Please try later !");
        	return new JoinResponse(
        			JoinResponse.Status.BUSY,
        			"Master node is Busy and cannot accept any new Join requests now, Please try later !");
        }
       
        joinLock_ = false;
        StringBuilder response = new StringBuilder();
        
        BigInteger hashKey = null;
        try {
            hashKey = Utils.sha1BigInt(url);
        }
        catch (NoSuchAlgorithmException e){
        	return new JoinResponse(JoinResponse.Status.ERROR, response.toString());
        }

        NodeInfo successor = successor(hashKey, response);
        
        globalNodeCount_++;
        Integer gc = globalNodeCount_;
        
        // computing the finger table of the new node, which is joining the ring
        String[] ft = computeFingerTableFor(hashKey);
        
        ChordInterface successorNode;
        NodeInfo nodeInfo;
        String predURL;
        try {
        	// we should update the predecessor information in the new node's successor
        	Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(successor.nodeURL_), rmiPort_);
            successorNode = (ChordInterface) registry.lookup(successor.nodeURL_);
            predURL = closestPrecedingNode(hashKey, response);
            nodeInfo = new NodeInfo(url, hashKey, gc);
            successorNode.notify(nodeInfo);
            System.out.println("Notifying the successor {"
            		+ successorNode.getMyInfo().nodeURL_ + "} of new node {"
            		+ nodeInfo.nodeURL_ + "} to update its predecessor.");
            response.append("Notifying the successor {"
            		+ successorNode.getMyInfo().nodeURL_ + "} of new node {"
            		+ nodeInfo.nodeURL_ + "} to update its predecessor.");
        }
        catch(Exception e){
        	return new JoinResponse(JoinResponse.Status.ERROR, response.toString());
        }
        
        ChordInterface predecessorNode;
        try {
        	// we should also update the successor pointer of the new node's predecessor
        	Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(predURL), rmiPort_);
            predecessorNode = (ChordInterface) registry.lookup(predURL);
            predecessorNode.updateSuccessor(nodeInfo);
            System.out.println("Notifying the predecessor {"
            		+ predecessorNode.getMyInfo().nodeURL_ + "} of new node {"
            		+ nodeInfo.nodeURL_ + "} to update its successor.");
            response.append("Notifying the predecessor {"
            		+ predecessorNode.getMyInfo().nodeURL_ + "} of new node {"
            		+ nodeInfo.nodeURL_ + "} to update its successor.");
        }
        catch(Exception e){
        	return new JoinResponse(JoinResponse.Status.ERROR, response.toString());
        }
		
        ft[0] = successorNode.getMyInfo().nodeURL_;
        response.append("Computed finger table for the new Node !");

        return new JoinResponse(JoinResponse.Status.DONE, response.toString(), nodeInfo, successor.nodeURL_, predURL, ft);
    }

    // we need to reorganize the finger table after the new node joins
    public void redistributeFingerTables(StringBuilder response, NodeInfo startNodeInfo, int iteration) {
    	
    	if(iteration <= 0) {
    		return;
    	}

    	// we will just be adjusting the fingers of all the nodes in the ring
		try {
			
			Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(startNodeInfo.nodeURL_), rmiPort_);
			ChordInterface startNode = (ChordInterface) registry.lookup(startNodeInfo.nodeURL_);
			startNode.fixFingers();
			response.append("Recomputed the finger tables of the node {")
					.append(startNodeInfo.nodeURL_)
					.append("} after new node has joined !");
			logger.info("Recomputed the finger tables of the node {" + startNodeInfo.nodeURL_
					+ "} after new node has joined !");
			
			redistributeFingerTables(response, startNode.getThisPredecessor(), --iteration);
				
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
        
    }
    
    // we need to reorganize the keys information after the new node joins
    public void redistributeKeys(NodeInfo successorInfo) {
    	
		try {
			
			// after a new node has joined, keys which earlier belonged to this
			// node might belong to the next node, i.e., the successor in the
			// ring. We need to slide these keys to the next node.
			// Since only the keys which are neighbors to the new node might
			// need keys to be redistributed, we can just do this for them
			Registry sregistry = LocateRegistry.getRegistry(Utils.getHostFromURL(successorInfo.nodeURL_), rmiPort_);
			ChordInterface succNode = (ChordInterface) sregistry.lookup(successorInfo.nodeURL_);

			Registry nregistry = LocateRegistry.getRegistry(Utils.getHostFromURL(succNode.getThisPredecessor().nodeURL_), rmiPort_);
			ChordInterface newNode = (ChordInterface) nregistry.lookup(succNode.getThisPredecessor().nodeURL_);
			
			Hashtable<String, String> keyStore = succNode.getKeyStore();
			HashMap<String, String> keysToBeMoved = new HashMap<String, String>();
			StringBuilder sb = new StringBuilder();
			StringBuilder temp = new StringBuilder();
			for(String key : keyStore.keySet()) {
				if(!masterNode_.successor(Utils.sha1BigInt(key), temp).nodeURL_.equals(successorInfo.nodeURL_)) {
					keysToBeMoved.put(key, keyStore.get(key));
					sb.append(key).append(",");
				}
			}
			for(String key: keysToBeMoved.keySet()) {
				newNode.insertKey(key, keysToBeMoved.get(key));
				succNode.removeKey(key);
			}
			logger.info("Moving the keys {" + sb.toString() + "} from {"
					+ succNode.getMyInfo().nodeURL_ + "} to {"
					+ newNode.getMyInfo().nodeURL_
					+ "} as a part of redistribution of keys !");
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        
    }
    
    @Override
    /*
     * looks up for a word in the current node
     */
    public String lookup(String word) throws RemoteException {
        return dictionary_.get(word);
    }
    
    @Override
    /*
    * Finds the successor who holds the key values for a given hash of the key
    */
    public NodeInfo successor(BigInteger key, StringBuilder logTrace) throws RemoteException {

		if (getMyInfo().nodeURL_.equals(getThisSuccessor().nodeURL_)) {
			// if there is only one node in the ring
			logTrace.append("Me and my successor are same, so I am returning myself {" + getMyInfo().nodeURL_ + "}");
			return getMyInfo();
		}

		if (isBetween(this.getMyInfo().nodeId_, getThisSuccessor().nodeId_, key)
				|| key.compareTo(getThisSuccessor().nodeId_) == 0) {
			// if the key is in between me and my successor
			logTrace.append("Key is between me and my successor, so I am returning my successor {" + getThisSuccessor().nodeURL_ + "}");
			return getThisSuccessor();
		} else {
			// find the closest predecessor of the key and get the successor
			String node = closestPrecedingNode(key, logTrace);
			logTrace.append("Found closest prdecessor to the key as {" + node + "}");
			if (node.equals(this.getMyInfo().nodeURL_)) {
				try {
                    Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(getThisSuccessor().nodeURL_), rmiPort_);
                    ChordInterface successorNode = (ChordInterface) registry.lookup(getThisSuccessor().nodeURL_);
                    logTrace.append("Finding the successor to the closest predecessor {" + node + "}");
                    return successorNode.successor(key, logTrace);
                } catch(Exception e){
                	logTrace.append(e.getMessage());
                    logger.severe(e.getMessage());
                }
			}
            ChordInterface closestPrecNode = null;
			try {
				closestPrecNode = (ChordInterface) LocateRegistry.getRegistry(Utils.getHostFromURL(node), rmiPort_).lookup(node);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
			logTrace.append("Finding the successor to the closest predecessor {" + closestPrecNode.getMyInfo().nodeURL_ + "}");
            return closestPrecNode.successor(key, logTrace);
		}

    }

    @Override
    /*
    * Gets the closest preceding node of the given hash of the key 
    */
    public String closestPrecedingNode(BigInteger key, StringBuilder logTrace) throws RemoteException {

    	try {

    		if(masterNode_.getMyInfo().nodeURL_.equals(masterNode_.getThisSuccessor().nodeURL_)) {
    			// if there is only one node in the ring
    			logTrace.append("Me and my successor are same, so I am returning myself {" + masterNode_.getMyInfo().nodeURL_ + "} as closest preceding node.");
    			return masterNode_.getMyInfo().nodeURL_;
    		}
    		if(isBetween(masterNode_.getMyInfo().nodeId_, masterNode_.getThisSuccessor().nodeId_, key) ||
    				masterNode_.getMyInfo().nodeId_.equals(key)) {
    			// if the key is in between me and my successor
    			logTrace.append("Key is between master and its successor, so I am returning master {" + masterNode_.getMyInfo().nodeURL_ + "} as predecessor.");
    			return masterNode_.getMyInfo().nodeURL_;
    		}

    		// locate the closest predecessor
    		Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(masterNode_.getThisSuccessor().nodeURL_), rmiPort_);
    		ChordInterface currNode = (ChordInterface) registry.lookup(masterNode_.getThisSuccessor().nodeURL_);
    		while(!currNode.getMyInfo().nodeId_.equals(masterNode_.getMyInfo().nodeId_)) {
    			if(isBetween(currNode.getMyInfo().nodeId_, currNode.getThisSuccessor().nodeId_, key) ||
    					currNode.getMyInfo().nodeId_.equals(key)) {
    				logger.info("Closest Preceding node for key {" + key +"} is {" + currNode.getMyInfo().nodeURL_ +"}");
    				logTrace.append("Closest Preceding node for key {" + key +"} is {" + currNode.getMyInfo().nodeURL_ +"}");
    				return currNode.getMyInfo().nodeURL_;
    			}
    			Registry nregistry = LocateRegistry.getRegistry(Utils.getHostFromURL(currNode.getThisSuccessor().nodeURL_), rmiPort_);
    			currNode = (ChordInterface) nregistry.lookup(currNode.getThisSuccessor().nodeURL_);
    		}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	
    	logTrace.append("Closest Preceding node for key {" + key +"} is {" + masterNode_.getMyInfo().nodeURL_ +"}");
    	logger.info("Closest Preceding node for key {" + key +"} is {" + masterNode_.getMyInfo().nodeURL_ +"}");
    	return masterNode_.getMyInfo().nodeURL_;
	}

    /*
     * Helper method to check if the third argument is in between (startKey, endKey)
     */
	public boolean isBetween(BigInteger startKey, BigInteger endKey, BigInteger findKey) throws RemoteException {
		if (startKey.compareTo(endKey) < 0) {
			if (findKey.compareTo(startKey) > 0 && findKey.compareTo(endKey) < 0) {
				return true;
			}
		} else if (startKey.compareTo(endKey) > 0) {
			if (findKey.compareTo(endKey) < 0 || findKey.compareTo(startKey) > 0) {
				return true;
			}
		}
		return false;
	}
	
    @Override
    /*
     * method to indicate the master node that a join has been done
     */
    public void join_done(NodeInfo newNode) throws RemoteException {
    	if(!isMasterNode_) {
    		logger.severe("I am not a master to serve join_done request !!!");
    		return;
    	}
    	
    	StringBuilder response = new StringBuilder();
        ChordInterface currNode;
		try {
			// we need to reorganize the finger table and the keys information
			// after the new node joins
			Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(newNode.nodeURL_), rmiPort_);
			currNode = (ChordInterface) registry.lookup(newNode.nodeURL_);
			redistributeFingerTables(response, currNode.getThisPredecessor(), globalNodeCount_-1);
			redistributeKeys(currNode.getThisSuccessor());
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
        
        response.append("Updating the status of the response to DONE")
				.append(System.getProperty("line.separator"));
        
        logger.info(response.toString());
        joinLock_ = true;
    }

    @Override
    /*
     * inserts a new word and its meaning into the current node
     */
    public void insertKey(String word, String meaning) throws RemoteException {
        dictionary_.put(word,meaning);
    }
    
    @Override
    /*
     * removes a word and its meaning from the current node 
     */
    public void removeKey(String word) throws RemoteException {
        dictionary_.remove(word);
    }

    @Override
    /*
     * called when someone notifies that this is their prdecessor
     */
    public void notify(NodeInfo predecessor) throws RemoteException {
    	logger.info("Updating the prdecessor of {"
    			+ this.getMyInfo().nodeURL_ + "} to {" + 
    			predecessor.nodeURL_ + "}");
    	myPredecessor_ = predecessor;
    }

    @Override
    public NodeInfo getMyInfo() throws RemoteException {
        return myInfo_;
    }
    
    @Override
    public Hashtable<String, String> getKeyStore() throws RemoteException {
    	return dictionary_;
    }
    
    @Override
    /*
     * called to update the current node successor 
     */
    public void updateSuccessor(NodeInfo successor) throws RemoteException {
        mySuccessor_ = successor;
        fingerTable_[0] = successor.nodeURL_;
        logger.info("Updating the successor of {" + myInfo_.nodeURL_ 
        		+ "} to {" + successor.nodeURL_ + "}");
    }
    
    @Override
    public NodeInfo getThisPredecessor() throws RemoteException {
    	return myPredecessor_;
    }
    
    @Override
    public NodeInfo getThisSuccessor() throws RemoteException {
    	return mySuccessor_;
    }

    @Override
    /*
     * recomputes the finger tables of the node 
     */
    public void fixFingers() throws RemoteException {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < fingerTable_.length; ++i){
            fingerTable_[i] = successor(getMyInfo().nodeId_.add(Utils.power(2,i)), sb).nodeURL_;
        }
    }
    
    @Override
    /*
     * finds the node which is actually responsible for holding the key 
     */
    public FindNodeResponsePair find_node(String key, boolean needTrace) throws RemoteException {
    	
    	if(!isMasterNode_) {
    		logger.severe("I am not a master to serve find_node request !!!");
    		return null;
    	}
    	
    	StringBuilder response = new StringBuilder();
    	NodeInfo successor = null;
    	
    	try {
    		successor = masterNode_.successor(Utils.sha1BigInt(key), response);
    		if(needTrace) {
    			response.append("find_node traced that the successor of the data with key {" + key + "} is {" + successor.nodeURL_ + "}")
    					.append(System.getProperty("line.separator"));
    			}
    		
    		logger.info("Node holding the data for key {" + key + "} is {"+ successor.nodeURL_ +"}");
    		
    	} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			if(needTrace) response.append(e.getMessage())
					.append(System.getProperty("line.separator"));
		}
    	
    	return new FindNodeResponsePair(successor.nodeId_, successor.nodeNum_, 
    			successor.nodeURL_, response.toString());
    	
    }
    
    @Override
    /*
     * method to print the node structure in a custom format 
     */
	public String getFormattedNodeDetails(boolean withFingers) throws RemoteException {
    	
		StringBuilder fd = new StringBuilder();
		try {
			fd.append("Node ID: ").append(getMyInfo().nodeNum_).append(", ")
					.append(System.getProperty("line.separator"))
					.append("URL: ").append(getMyInfo().nodeURL_).append(", ")
					.append(System.getProperty("line.separator"))
					.append("160-bit key: ").append(Utils.sha1String(getMyInfo().nodeURL_)).append(", ")
					.append(System.getProperty("line.separator"))
					.append("Successor: ").append(getThisSuccessor().nodeURL_).append(", ")
					.append(System.getProperty("line.separator"))
					.append("Predecessor: ").append(getThisPredecessor().nodeURL_).append(", ")
					.append(System.getProperty("line.separator"));
			
			if(withFingers) {
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < 160; ++i) {
					if(fingerTable_[i] != null && !fingerTable_[i].isEmpty()) {
						sb.append(fingerTable_[i]).append(",");
					}
				}
				fd.append("Finger Table: ").append(sb.toString())
					.append(System.getProperty("line.separator"));
			}
			
			fd.append("Number of Entries: ").append(getKeyStore().size())
				.append(System.getProperty("line.separator"));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return fd.toString();
	}
   
    @Override
    /*
     * prints the structure of ring with fingers, starting from node0 
     */
    public String printRingStructure() throws RemoteException {
    	
    	// we will accumulate information from all nodes and then print the ring structure
    	StringBuilder ringStructure = new StringBuilder();
    	try {
			
			ringStructure.append(masterNode_.getFormattedNodeDetails(true))
					.append(System.getProperty("line.separator"));
			
			NodeInfo currNodeInfo = masterNode_.getThisSuccessor();
			
			while(!currNodeInfo.nodeId_.equals(masterNode_.getMyInfo().nodeId_)) {
				Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(currNodeInfo.nodeURL_), rmiPort_);
				ChordInterface currNode = (ChordInterface) registry.lookup(currNodeInfo.nodeURL_);
				ringStructure.append(currNode.getFormattedNodeDetails(true))
							 .append(System.getProperty("line.separator"));
				currNodeInfo = currNode.getThisSuccessor();
				
			}
			logger.info("Ring structure is -> " + ringStructure.toString());
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	return ringStructure.toString();
    }
    
    @Override
    /*
     * prints the structure of ring without fingers, starting from node0 
     */
    public String printRingStructureWithoutFingers() throws RemoteException {
    	
    	// we will accumulate information from all nodes and then print the ring structure
    	StringBuilder ringStructure = new StringBuilder();
    	try {
			
			ringStructure.append(masterNode_.getFormattedNodeDetails(false))
					.append(System.getProperty("line.separator"));
			
			NodeInfo currNodeInfo = masterNode_.getThisSuccessor();
			
			while(!currNodeInfo.nodeId_.equals(masterNode_.getMyInfo().nodeId_)) {
				Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(currNodeInfo.nodeURL_), rmiPort_);
				ChordInterface currNode = (ChordInterface) registry.lookup(currNodeInfo.nodeURL_);
				ringStructure.append(currNode.getFormattedNodeDetails(false))
							 .append(System.getProperty("line.separator"));
				currNodeInfo = currNode.getThisSuccessor();
				
			}
			logger.info("Ring structure is -> " + ringStructure.toString());
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	return ringStructure.toString();
    }
    
    /*
     * computes finger table of the new node which is joining the ring 
     */
    public String[] computeFingerTableFor(BigInteger id) throws RemoteException {
        String[] fingerTable = new String[160];
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<160;i++){
            fingerTable[i] = successor(id.add(Utils.power(2,i)), sb).nodeURL_;
        }
        return fingerTable;
    }

    public static void main(String[] args) {

    	if(args.length != 3) {
    		System.out.println("Incorrect number of arguments. Please provide the following two arguments <currentNodeURL> <masterNodeURL> <rmiport>");
    		return;
    	}

    	String nodeURL = args[0];
    	MASTER_NODE_URL = args[1];
    	rmiPort_ = Integer.parseInt(args[2]);
    	
    	
        try {
        	ChordInterface node = new Node(nodeURL);
        	Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(nodeURL), rmiPort_);
            registry.rebind(nodeURL, node);
            
            if(!nodeURL.equals(MASTER_NODE_URL)) {
            	Registry registry1 = LocateRegistry.getRegistry(Utils.getHostFromURL(MASTER_NODE_URL), rmiPort_);
            	ChordInterface master = (ChordInterface) registry1.lookup(MASTER_NODE_URL);
	            master.join_done(node.getMyInfo());
            }

        } catch (RemoteException e){
            e.printStackTrace();
        } catch (NotBoundException e) {
			e.printStackTrace();
		}

    }
}
