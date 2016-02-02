import java.io.Serializable;
import java.math.BigInteger;

/**
 * Internal class to represent the details of a node i.e., url, id and the
 * number (user friendly representation of node).
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
public class NodeInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	public String nodeURL_;
    public BigInteger nodeId_;
    public Integer nodeNum_;

    public NodeInfo(String url,BigInteger id,Integer num){
        nodeURL_ = url;
        nodeId_ = id;
        nodeNum_ = num;
    }

}