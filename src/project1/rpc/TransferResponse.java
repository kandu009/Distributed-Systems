package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class TransferResponse extends Response {

    TransferResponse(Integer status){
        super("Transfer");
        super.update_status(status);
    }
}
