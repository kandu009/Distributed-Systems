import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Client class which is used to try and test the DHT functionality
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
public class Client {

	private static final String WORDS_FILE_PATH = "words.txt";
	private static String MASTER_NODE_URL = "node0";
	private static Logger logger = ClientLogger.logger();
	
	private static HashMap<String, String> wordMeaningStore_ = new HashMap<String, String>();
	private static int rmiPort_ = 1099;

	/*
	 * Loads all words and meanings initially
	 */
	public static void loadWordMeaningPairs() {
		try {
			File file = new File(WORDS_FILE_PATH);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] toks = line.trim().split(":");
				if (toks.length != 2) {
					continue;
				}
				wordMeaningStore_.put(toks[0].trim(), toks[1].trim());
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Inserts all the words into a DHT 
	 */
	private static void insertWordsInDHT(ChordInterface node0) {

		for(String word : wordMeaningStore_.keySet()) {
			FindNodeResponsePair fp;
			try {
				fp = node0.find_node(word, true);
			} catch (RemoteException e1) {
				e1.printStackTrace();
				return;
			}
			try {
				logger.info("Insert find_node trace for word {" + word + "} is : " + fp.response_);
				Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(fp.nodeUrl_), rmiPort_);
				ChordInterface insertNode = (ChordInterface) registry.lookup(fp.nodeUrl_);
				insertNode.insertKey(word, wordMeaningStore_.get(word));
				logger.info("Successfully inserted word {" + word + "} in the DHT.");
			} catch (RemoteException | NotBoundException e) {
				logger.severe("Could not insert word {" + word + "} in DHT.");
				e.printStackTrace();
			}
		}
        
	}
	
	/*
	 * Inserts all the words into a DHT 
	 */
	private static void insertWordInDHT(ChordInterface node0, String word, String meaning, boolean withTrace) {

		FindNodeResponsePair fp;
		try {
			fp = node0.find_node(word, withTrace);
			if(withTrace) {
				logger.info("Lookup find_node trace for word {" + word + "} is : " + fp.response_);
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(fp.nodeUrl_), rmiPort_);
			ChordInterface insertNode = (ChordInterface) registry.lookup(fp.nodeUrl_);
			insertNode.insertKey(word, meaning);
			logger.info("Successfully inserted word {" + word + "} in the DHT.");
		} catch (RemoteException | NotBoundException e) {
			logger.severe("Could not insert word {" + word + "} in DHT.");
		}
        
	}
	
	/*
	 * method to lookup a specific work in a DHT
	 */
	private static void lookupinDHT(String word, ChordInterface node0, boolean withTrace) {

		String meaning = new String();
		
		try {
			FindNodeResponsePair fp = node0.find_node(word, withTrace);
			if(withTrace) {
				logger.info("find_node trace for word {" + word + "} is : ");
				logger.info(fp.response_);
				logger.info("Looking the meaning of word in {" + fp.nodeUrl_ + "}");
			}
			Registry registry = LocateRegistry.getRegistry(Utils.getHostFromURL(fp.nodeUrl_), rmiPort_);
			ChordInterface lookupNode = (ChordInterface) registry.lookup(fp.nodeUrl_);
			meaning = lookupNode.lookup(word);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
		if(meaning == null || meaning.isEmpty()) {
			logger.severe("Could not find any meaning for the word {" + word + "}");
		} else {
			logger.info("Meaning of word {" + word + "} is {" + meaning +"}");
		}
	}

	/*
	 * Helper to print the user options list 
	 */
	private static void printChoiceList() {
		StringBuilder sb = new StringBuilder();
		sb.append("Please enter your choice:").append(System.getProperty("line.separator"))
			.append("1. print ring structure").append(System.getProperty("line.separator"))
			.append("2. print ring structure without fingerTables").append(System.getProperty("line.separator"))
			.append("3. lookup word without log trace").append(System.getProperty("line.separator"))
			.append("4. lookup with log trace").append(System.getProperty("line.separator"))
			.append("5. insert with log trace").append(System.getProperty("line.separator"))
			.append("6. insert without log trace").append(System.getProperty("line.separator"))
			.append("7. Exit").append(System.getProperty("line.separator"));
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args) {
		
		if(args.length != 2) {
    		System.out.println("Incorrect number of arguments. Please provide the following arguments <masterNodeURL> <rmiPort>");
    		return;
    	}
    	
    	MASTER_NODE_URL = args[0].trim();
    	rmiPort_ = Integer.parseInt(args[1]);

    	System.setSecurityManager(new RMISecurityManager());
		Registry registry;
		ChordInterface masterNode;
		try {
			registry = LocateRegistry.getRegistry(Utils.getHostFromURL(MASTER_NODE_URL), rmiPort_);
			masterNode = (ChordInterface) registry.lookup(MASTER_NODE_URL);
		} catch (RemoteException e1) {
			logger.severe("Could not locate remote RMI registry, Exiting !!!");
			return;
		} catch (NotBoundException e) {
			logger.severe("Could not find any binding with name {" + MASTER_NODE_URL + "}, Exiting !!!");
			return;
		}
		
		// load all the dictionary words
		loadWordMeaningPairs();
		
		// insert dictionary words and meanings into DHT
		insertWordsInDHT(masterNode);
		
		String inputChoice = new String();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(true) {
			
			printChoiceList();
			
			try {
				inputChoice = br.readLine();
				switch(Integer.parseInt(inputChoice)) 
				{
					case 1: { 
								System.out.println(masterNode.printRingStructure());
								break; 
							}
					case 2: {
								System.out.println(masterNode.printRingStructureWithoutFingers());
								break;
							}
					case 3: {
								try {
									logger.info("Enter the word to lookup: ");
									String word = br.readLine();
									lookupinDHT(word, masterNode, false);
								} catch (IOException e) {
									System.out.println("Error while reading the input. Please try again !");
								}
								break; 
							}
					case 4: {
								try {
									logger.info("Enter the word to lookup: ");
									String word = br.readLine();
									lookupinDHT(word, masterNode, true);
								} catch (IOException e) {
									System.out.println("Error while reading the input. Please try again !");
								}
								break; 
							}
					case 5: {
								logger.info("Enter the word to insert: ");
								String word = br.readLine();
								logger.info("Enter the meaning of the word to insert: ");
								String meaning = br.readLine();
								insertWordInDHT(masterNode, word, meaning, true);
								break;
							}
					case 6: {
								logger.info("Enter the word to insert: ");
								String word = br.readLine();
								logger.info("Enter the meaning of the word to insert: ");
								String meaning = br.readLine();
								insertWordInDHT(masterNode, word, meaning, false);
								break;
							}
					case 7: { 
								System.exit(0);
								break; 
							}
					default: { break; }
				}

			} catch (Exception e) {
				System.out.println("Invalid input entered {" + inputChoice +"} try again !");
			}
			
		}
		
	}

}
