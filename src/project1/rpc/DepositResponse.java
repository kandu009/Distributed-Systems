package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class DepositResponse extends Response {

    DepositResponse(Integer status){
        super("Deposit");
        super.update_status(status);
    }
}
