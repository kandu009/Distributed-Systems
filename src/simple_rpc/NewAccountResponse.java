package simple_rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class NewAccountResponse extends Response {

    Integer accId;
    NewAccountResponse(Integer accId){
        super("NewAccount");
        this.accId = accId;
    }
}
