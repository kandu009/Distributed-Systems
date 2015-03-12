package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class NewAccountRequest extends Request{

    String firstname;
    String lastname;
    String address;


    NewAccountRequest(String firstname, String lastname, String address){
        super("NewAccount");
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
    }
}
