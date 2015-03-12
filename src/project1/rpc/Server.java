package project1.rpc;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Charandeep on 2/11/15.
 */
public class Server{

    static Integer curr_Acc = 1;
    /** TODO: RKNOTE
     * Shouldn't this be a {@link Hashtable} as mentioned in the Assignment?
     */
    static ConcurrentHashMap acc_dB;
    static Logger logger;
    public static void main(String[] args) throws IOException,ClassNotFoundException{
        logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("serverLogfile");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        acc_dB = new ConcurrentHashMap(200);
        Set acc_Num = new HashSet();
        System.out.println("Creating a Server on 1234...");
        /** TODO: RKNOTE
         * Should the port be configurable? 
         */
        ServerSocket bank = new ServerSocket(1234);
        System.out.println("Server is waiting for the client...");
        try {
            while(true){
                new ServerThread(bank.accept()).start();
            }
        }
        finally{
            bank.close();
        }


    }

    private static class ServerThread extends Thread {
        private Socket socket;
        public ServerThread(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            try{
                InputStream ins = socket.getInputStream();
                ObjectInputStream req = new ObjectInputStream(ins);
                Request r = (Request)req.readObject();
                OutputStream outs = socket.getOutputStream();
                ObjectOutputStream res = new ObjectOutputStream(outs);
                switch(r.req_type) {
                    case Request.NewAccount:
                        NewAccountRequest nr = (NewAccountRequest) r;
                        logger.info("NewAccount " + nr.firstname + " " + nr.lastname + " " + nr.address + " " + curr_Acc);
                        /** TODO: RKNOTE
                         * I don't think we will need this synchronized if we use HashTable
                         */
                        synchronized (this) {
                            AccountInfo na = new AccountInfo(curr_Acc, nr.firstname, nr.lastname, nr.address);
                            acc_dB.put(curr_Acc, na);
                            NewAccountResponse nrs = new NewAccountResponse(curr_Acc);
                            res.writeObject(nrs);
                            curr_Acc++;
                        }
                        break;
                    case Request.Deposit:
                        DepositRequest dr = (DepositRequest) r;
                        AccountInfo nb = (AccountInfo) acc_dB.get(dr.accId);
                        nb.updateBalance(dr.num);
                        /** TODO: RKNOTE
                         * If you want to use the ConcurrentHashMap, this should also probably be inside synchronized block?
                         */
                        acc_dB.put(dr.accId, nb);
                        DepositResponse drs = new DepositResponse(1);
                        res.writeObject(drs);
                        logger.info("Deposit "+dr.accId+" "+"100 "+drs.get_status());
                        /** TODO: RKNOTE
                         * Not sure what this 100 means in the logs, may be I am missing something
                         */
                        break;
                    case Request.Withdraw:
                        WithdrawRequest wr = (WithdrawRequest) r;
                        AccountInfo nc = (AccountInfo) acc_dB.get(wr.accId);
                        WithdrawResponse wrs;
                        synchronized (this) {
                            if (nc.getBalance() < wr.num) {
                                wrs = new WithdrawResponse(0);
                            } else {
                                nc.updateBalance(-wr.num);
                                acc_dB.put(wr.accId, nc);
                                wrs = new WithdrawResponse(1);
                            }
                        }
                        res.writeObject(wrs);
                        logger.info("Withdraw "+wr.accId+" "+"100 "+wrs.get_status());
                        break;
                    case Request.GetBalance:
                        GetBalanceRequest gb = (GetBalanceRequest) r;
                        AccountInfo nd = (AccountInfo) acc_dB.get(gb.accId);
                        Integer bal = nd.getBalance();
                        GetBalanceResponse gbs = new GetBalanceResponse(bal);
                        res.writeObject(gbs);
                        logger.info("GetBalance "+gb.accId+" "+ bal);
                        break;

                    case Request.Transfer:
                        TransferRequest tr = (TransferRequest) r;
                        AccountInfo ne = (AccountInfo) acc_dB.get(tr.sAccId);
                        AccountInfo ne2 = (AccountInfo) acc_dB.get(tr.tAccId);
                        TransferResponse trs;
                        synchronized (this){
                            if (ne.getBalance() < tr.num) {
                                trs = new TransferResponse(0);
                            } else {
                                ne.updateBalance(-tr.num);
                                acc_dB.put(tr.sAccId, ne);
                                ne2.updateBalance(tr.num);
                                acc_dB.put(tr.tAccId, ne2);
                                trs = new TransferResponse(1);
                            }
                        }
                        res.writeObject(trs);
                        logger.info("Transfer "+tr.sAccId+" "+ tr.tAccId+" "+100+" "+trs.get_status());
                        break;
                }
            }
            catch(ClassNotFoundException cne){
                cne.printStackTrace();
            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    //log("Couldn't close a socket, what's going on?");
                }
            }
        }
    }

}
