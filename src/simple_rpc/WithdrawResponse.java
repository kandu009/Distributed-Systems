package simple_rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class WithdrawResponse extends Response {

    WithdrawResponse(Integer status){
        super("Withdraw");
        super.update_status(status);
    }
}
