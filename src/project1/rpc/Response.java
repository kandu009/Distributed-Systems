package project1.rpc;
import java.io.Serializable;

/**
 * Created by Charandeep on 2/11/15.
 */
public class Response implements Serializable{

    String res;
    private String _status;
    public enum status{OK,FAIL} ;
    Response(String res){
        this.res = res;
    }
    public void update_status(Integer s){
        if(s==0){
            this._status = "FAIL";
        }
        else if(s==1){
            this._status = "OK";
        }
    }
    public String get_status(){
        return this._status;
    }
}
