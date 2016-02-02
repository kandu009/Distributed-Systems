package simple_rpc;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Charandeep on 2/11/15.
 */
public class ClientB {

    static String hostname;
    static Integer port;
    static ObjectInputStream ins;
    static ObjectOutputStream outs;
    static Logger logger;

    public static void main(String[] args) throws IOException,InterruptedException{

        logger = Logger.getLogger("MyLog1");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("clientLogfile");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(args.length!=4){
            throw new RuntimeException("serverHostname severPortnumber threadCount iterationCount as arguments");
        }

        hostname = args[0];
        port = Integer.parseInt(args[1]);



        Integer numThreads = Integer.parseInt(args[2]);
        Integer iterCount=Integer.parseInt(args[3]);


        ClientBThread[] threads;
        threads = new ClientBThread[100];
        int[] accs;
        accs = new int[100];
        Integer totalBalance = 0;
        for(int i=0;i<100;i++){
            accs[i]=newAccountRequest("Bank","acc"+i,"street"+i);
        }
        System.out.println("100 Accounts created!\n");

        for(int i=0;i<100;i++){
            deposit(accs[i]);
        }
        System.out.println("100 dollars in each account\n");


        for(int i=0;i<100;i++){
            totalBalance+=getBalance(accs[i]);
        }
        System.out.println("Total balance is "+totalBalance+"\n");

        System.out.println("Transfer threads starting...");
        for(int i=0;i< numThreads;i++){
            threads[i]=new ClientBThread(iterCount,accs);
            threads[i].start();
            threads[i].join();
        }

        System.out.println("Transfer threads executed.");

        totalBalance = 0;


        for(int i=0;i<100;i++){
            totalBalance+=getBalance(accs[i]);
        }
        System.out.println("Final total balance is "+totalBalance+"\n");

    }

    private static class ClientBThread extends Thread{

        Integer iterCount;
        int[] accs;
        public ClientBThread(Integer iterationCount, int[] acs){
            iterCount = iterationCount;
            accs = acs;
        }

        public static int getRandom(int[] array) {
            int rnd = new Random().nextInt(array.length);
            return array[rnd];
        }
        public void run(){
            Integer acc1;
            Integer acc2;
            try{
                for(int i=0;i<iterCount;i++) {
                    acc1 = getRandom(accs);
                    acc2 = getRandom(accs);
                    transfer(acc1, acc2);
                }

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }


    }
    public static int newAccountRequest(String firstname,String lastname,String address) throws IOException {

        NewAccountRequest nr = new NewAccountRequest(firstname, lastname, address);

        Socket socket = new Socket(hostname,port);

        OutputStream rawOut = socket.getOutputStream ();
         outs = new ObjectOutputStream(rawOut);

        outs.writeObject(nr);

        InputStream rawIn = socket.getInputStream ();
         ins = new ObjectInputStream(rawIn);
        NewAccountResponse nrs =null;
        try {
            nrs = (NewAccountResponse)ins.readObject();
        }
        catch(ClassNotFoundException cnf){
            cnf.printStackTrace();
        }
        finally {
            socket.close();
        }
        return nrs.accId;
    }

    public static int getBalance(Integer accId) throws IOException{
        GetBalanceRequest gb = new GetBalanceRequest(accId);

        Socket socket = new Socket(hostname,port);

        OutputStream rawOut = socket.getOutputStream ();
        outs = new ObjectOutputStream(rawOut);

        outs.writeObject(gb);

        InputStream rawIn = socket.getInputStream ();
        ins = new ObjectInputStream(rawIn);

        GetBalanceResponse gbs = null;
        try {
            gbs = (GetBalanceResponse)ins.readObject();
        }
        catch (ClassNotFoundException cnf){
            cnf.printStackTrace();
        }
        finally {
            socket.close();
        }

        return gbs.bal;
    }


    public static void transfer(Integer sAccId, Integer tAccId) throws IOException{

        TransferRequest tr = new TransferRequest(sAccId,tAccId,10);
        Socket socket = new Socket(hostname,port);

        OutputStream rawOut = socket.getOutputStream ();
        outs = new ObjectOutputStream(rawOut);
        outs.writeObject(tr);

        InputStream rawIn = socket.getInputStream ();
        ins = new ObjectInputStream(rawIn);
        TransferResponse ts = null;
        try{
            ts = (TransferResponse)ins.readObject();
            if(ts.get_status()=="FAIL"){
                logger.info(ts.get_status()+" "+sAccId+" "+tAccId);
            }
        }
        catch (ClassNotFoundException cnf){
            cnf.printStackTrace();
        }
        finally {
            socket.close();
        }
    }

    public static void deposit(Integer accId) throws IOException{
        DepositRequest dr = new DepositRequest(accId,100);

        Socket socket = new Socket(hostname,port);

        OutputStream rawOut = socket.getOutputStream ();
        outs = new ObjectOutputStream(rawOut);
        outs.writeObject(dr);

        InputStream rawIn = socket.getInputStream ();
        ins = new ObjectInputStream(rawIn);
        DepositResponse drs = null;
        try {
            drs = (DepositResponse)ins.readObject();
        }
        catch (ClassNotFoundException cnf){
            cnf.printStackTrace();
        }
        finally {
            socket.close();
        }
    }

}
