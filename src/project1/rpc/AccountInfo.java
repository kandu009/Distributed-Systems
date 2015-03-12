package project1.rpc;
/**
 * Created by Charandeep on 2/11/15.
 */
public class AccountInfo {

    public Integer acc_Num;
    private Integer balance;
    private String firstname;
    private String lastname;
    private String address;

    AccountInfo(Integer acc_Num, String firstname, String lastname, String address){
        this.acc_Num = acc_Num;
        this.balance = 0;
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
    }

    public int getBalance(){
        return this.balance;
    }
    public void updateBalance(int num){
        this.balance = this.balance+num;
    }

}
