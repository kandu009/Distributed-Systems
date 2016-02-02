package simple_rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class GetBalanceResponse extends Response{

    Integer bal;
    GetBalanceResponse(Integer num){
        super("GetBalance");
        this.bal = num;
    }
}
