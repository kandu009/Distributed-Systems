package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class DepositRequest extends Request {

    Integer accId;
    Integer num;
    DepositRequest(int accId, int num){
        super("Deposit");
        this.accId = accId;
        this.num = num;

    }

}
