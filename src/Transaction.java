import java.security.*;	
import java.util.ArrayList;


public class Transaction {
	
	public String transactionId;
	public PublicKey sender;
	public PublicKey receiver;
	
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs = new ArrayList<>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<>();
	
	private static int sequence = 0;
	
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
		
		this.sender = from;
		this.receiver = to;
		this.value = value;
		this.inputs = inputs;
	}
	public float getInputsValue(){
		float total = 0;
		for(TransactionInput i : inputs){
			if(i.UTXO == null)
				continue;
			total += i.UTXO.value;
		}
		return total;
	}
	
	public float getOutputsValue(){
		float total = 0;
		for(TransactionOutput o : outputs){
			total += o.value;
		}
		return total;
	}
	
	
	private String calculateHash(){
		
		sequence++;
		return StringUtil.applySha256(
			StringUtil.getStringFromKey(sender) +
			StringUtil.getStringFromKey(receiver) +
			Float.toString(value) + sequence
		);		
	}
	
	public boolean processTransaction(){
		
		if(verifySignature() == false){
			System.out.println("#Transaction signature failed to verify.");
			return false;
		}
		
		for(TransactionInput i : inputs){
			i.UTXO = RookieChain.UXTOs.get(i.transactionOutputId); 
		}
		
		if(getInputsValue() < RookieChain.minimumTransaction){
			
			System.out.println("#Transaction inputs to small: " +getInputsValue());
			return false;
			
		}
		
		float leftOver = getInputsValue() - value;
		transactionId = calculateHash();
		outputs.add(new TransactionOutput( this.receiver, value, transactionId));
		outputs.add(new TransactionOutput( this.sender, leftOver, transactionId));
			
		for(TransactionOutput o : outputs){
			RookieChain.UXTOs.put(o.id, o);
		}
		
		for(TransactionInput i : inputs){
			if(i.UTXO == null) 
				continue;
			RookieChain.UXTOs.remove(i.UTXO.id);
			
		}
		
		return true;
	}
	
	public void generateSignature(PrivateKey privateKey){
		
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receiver) + Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}
	
	public boolean verifySignature(){
		
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receiver) + Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	

}
