package simple_rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class TransferRequest extends Request {

    Integer sAccId;
    Integer tAccId;
    Integer num;
    TransferRequest(Integer sAccId, Integer tAccId, Integer num){
        super("Transfer");
        this.sAccId = sAccId;
        this.tAccId = tAccId;
        this.num = num;

    }
}
