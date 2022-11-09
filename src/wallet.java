import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class wallet {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransactionOutput> UXTOs = new HashMap<>();
	
	public wallet(){
		generateKeyPair();
	}
	
	public void generateKeyPair(){
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public float getBalance(){
		float total = 0;
		for(Map.Entry<String, TransactionOutput> item : RookieChain.UXTOs.entrySet()){
			TransactionOutput UXTO = item.getValue();
			
			if(UXTO.isMine(publicKey)){
				UXTOs.put(UXTO.id, UXTO);
				total += UXTO.value;
			}
		}
		return total;
	}
	
	public Transaction sendFunds(PublicKey receiver, float value){
		
		if(getBalance() < value){
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		float total = 0;
		
		for(Map.Entry<String, TransactionOutput> item : UXTOs.entrySet()){
			TransactionOutput UXTO = item.getValue();
			total += UXTO.value;
			inputs.add(new TransactionInput(UXTO.id));
			if(total > value)
				break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, receiver, value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input : inputs){
			UXTOs.remove(input.transactionOutputId);
		}
		return newTransaction;
		
	}

}
