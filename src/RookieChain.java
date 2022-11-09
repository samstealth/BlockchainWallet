import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;



public class RookieChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<>();
	public static HashMap<String, TransactionOutput> UXTOs = new HashMap<>(); 
	
	
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	public static wallet walletA;
	public static wallet walletB;
	public static Transaction genesisTransaction;
	

	public static void main(String[] args) {
		
		/*Block genesisBlock = new Block("Hi I'm your first Block.","0");
		System.out.println("Hash for block 1: " +genesisBlock.hash);
		
		Block secondBlock = new Block("Yo! I'm your second Block.",genesisBlock.hash);
		System.out.println("Hash for block 2: " + secondBlock.hash);
		
		Block thirdBlock = new Block("Hey! I'm your third Block.",secondBlock.hash);
		System.out.println("Hash for block 3: " +thirdBlock.hash); 
		
		blockchain.add(new Block("Hi I am your first Block.","0"));
		System.out.println("Trying to Mine Block 1...");
		blockchain.get(0).mineBlock(difficulty);
		
		
		blockchain.add(new Block("Yo! I am your second Block.",blockchain.get(blockchain.size()-1).hash));
		System.out.println("Trying to Mine Block 2...");
		blockchain.get(1).mineBlock(difficulty);
		
		
		blockchain.add(new Block("Hey! I am your third Block.",blockchain.get(blockchain.size()-1).hash));
		System.out.println("Trying to Mine Block 3...");
		blockchain.get(2).mineBlock(difficulty);
		
		
		System.out.println("\nBlockchain is Valid:  " +isChainValid());
		
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nThe Block Chain:  ");
		System.out.println(blockchainJson);*/
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		walletA = new wallet();
		walletB = new wallet();
		wallet coinbase = new wallet();
		
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId = "0";
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId));
		UXTOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		System.out.println("Creating and Mining Genesis Block...");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		Block block1 = new Block(genesis.hash);
		System.out.println("\nWallet A's balance is: "+walletA.getBalance());
		System.out.println("\nWalletA is attempting to send funds (40) to walletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWallet A's balance is: "+walletA.getBalance());
		System.out.println("\nWallet B's balance is: "+walletB.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("\nWallet A attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWallet A's balance is: "+walletA.getBalance());
		System.out.println("\nWallet B's balance is: "+walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB is attempting to send funds (20) to walletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
		System.out.println("\nWallet A's balance is: "+walletA.getBalance());
		System.out.println("\nWallet B's balance is: "+walletB.getBalance());
		
		isChainValid();
		
		
		/*System.out.println("Public and private Keys: ");
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		Transaction tran = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		tran.generateSignature(walletA.privateKey);
		
		System.out.println("Is Signature verified?");
		System.out.println(tran.verifySignature());*/
		
		
		
	}
	
	
	public static Boolean isChainValid(){
		
		Block currentBlock;
		Block previousBlock;
		
		String hashTarget = new String(new char[difficulty]).replace('\0','0');
		HashMap<String, TransactionOutput> tempUXTOs = new HashMap<>();
		tempUXTOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		for(int i = 1; i < blockchain.size(); i++){
		
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes are not equal");
				return false;
			}
			
			if(!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("Previous Hashes are not equal");
				return false;
			}
			
			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)){
				System.out.println("The Block hasn't been mined.");
				return false;
			}
			
			TransactionOutput tempOutput;
			for(int t = 0; t < currentBlock.trans.size(); t++){
				Transaction currentTransaction = currentBlock.trans.get(t);
				
				if(!currentTransaction.verifySignature()){
					System.out.println("#Signature on Transaction("+ t +") is Invalid");
					return false;
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()){
					System.out.println("#Inputs are not equal to outputs on Transaction("+ t +")");
					return false;
					
				}
				for(TransactionInput input : currentTransaction.inputs){
					tempOutput = tempUXTOs.get(input.transactionOutputId);
					
					
					if(tempOutput == null){
						System.out.println("#Referenced input on transaction ("+ t +") is Missing.");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value){
						System.out.println("#Referenced input transaction("+ t +")");
						return false;
					}
					
					tempUXTOs.remove(input.transactionOutputId);
				}
				for(TransactionOutput output : currentTransaction.outputs){
					tempUXTOs.put(output.id, output);
				}
				
				if(currentTransaction.outputs.get(0).receiver != currentTransaction.receiver){
					System.out.println("#Transaction("+ t +") output receiver is not who it should be.");
					return false;
				}
				if(currentTransaction.outputs.get(1).receiver != currentTransaction.sender){
					System.out.println("#Transaction("+ t +") output 'change' is not sender");
					return false;
				}
			}
			
		}
		System.out.println("BlockChain is Valid!");
		return true;
	}
	
	public static void addBlock(Block newBlock){
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

}