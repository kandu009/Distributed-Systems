import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils method which has some helper methods
 * 
 * @author rkandur
 * 
 * CSci5105 Spring 2015
 * Assignment# 7
 * name: <Ravali Kandur>, <Charandeep Parisineti>
 * student id: <5084769>, <5103173>
 * x500 id: <kandu009>, <paris102>
 * CSELABS machine: In README
 *
 */
public class Utils {

	/*
	 * method which gives the hashed value of the key using SHA1 as hex string
	 */
	public static String sha1String(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

	/*
	 * method which gives the hashed value of the key using SHA1 as BigInteger
	 */
    public static BigInteger sha1BigInt(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        BigInteger bi = new BigInteger(1, result);
        return bi;
    }
    
    /*
     * method to compute base^exponent
     */
    public static BigInteger power(Integer base, Integer exponent){
        BigInteger result = BigInteger.ONE;
        for(int i=1;i<=exponent;i++){
            result = result.multiply(BigInteger.valueOf(base));
        }
        return result;
    }
	
    /*
     * Utility method to get the host name from the URL
     */
    public static String getHostFromURL(String url) {
    	return url.split("/")[0];
    }
    
}
