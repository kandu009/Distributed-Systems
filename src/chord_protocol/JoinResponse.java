import java.io.Serializable;

/**
 * Created by Charandeep on 4/21/15.
 * 
 * Class which denotes the typical response received from server 
 * when a Join request is made
 * 
 * CSci5105 Spring 2015
 * Assignment# 7
 * name: <Ravali Kandur>, <Charandeep Parisineti>
 * student id: <5084769>, <5103173>
 * x500 id: <kandu009>, <paris102>
 * CSELABS machine: In README
 * 
 */
public class JoinResponse implements Serializable{

	private static final long serialVersionUID = 1L;

	public enum Status{
        BUSY,
        DONE, 
        ERROR,
        NONE;
    }

    public Status status;
    public String response;
    public NodeInfo newNodeInfo;
    public String successorURL;
    public String predecessorURL;
    public String[] fingerTable;

    public JoinResponse() {
    	status = Status.NONE;
    	response = new String();
    }
    
    public JoinResponse(Status s, String r) {
    	status = s;
    	response = r;
    }
    
    public JoinResponse(Status s, String r, NodeInfo info, String succ, String pred, String[] ft) {
    	status = s;
    	response = r;
    	newNodeInfo = info;
    	successorURL =succ;
    	predecessorURL = pred;
    	fingerTable = ft;
    }
    
}
