package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class WithdrawRequest extends Request {

    Integer accId;
    Integer num;

    WithdrawRequest(Integer accId, Integer num){
        super("Withdraw");
        this.accId = accId;
        this.num = num;
    }
}
