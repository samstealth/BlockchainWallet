import java.util.Date;
import java.util.ArrayList;

public class Block {
	
	public String hash;
	public String previousHash;
	//public String data;
	public String merkleRoot;
	public ArrayList<Transaction> trans = new ArrayList<>();
	private long timeStamp;
	private int nonce;
	
	//Block Constructor
	public Block(String previousHash){
		
		//this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}
	
	public String calculateHash(){
		
		String calculatedHash = StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		
		return calculatedHash;
	}
	
	public void mineBlock(int difficulty){
		
		merkleRoot = StringUtil.getMerkleRoot(trans);
		//String target =  new String(new char[difficulty]).replace('\0','0');
		String target = StringUtil.getDifficultyString(difficulty);
		
		while(!hash.substring(0, difficulty).equals(target)){
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " +hash);
	}
	
	public boolean addTransaction(Transaction transaction){
		
		if(transaction == null)
			return false;
		if(previousHash != "0"){
			if(transaction.processTransaction() != true){
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		
		trans.add(transaction);
		System.out.println("Transaction Successfully added to Block.");
		return true;
	}
	
	 

}
