package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class GetBalanceRequest extends Request {

    Integer accId;
    GetBalanceRequest(Integer accId){
        super("GetBalance");
        this.accId = accId;
    }
}
