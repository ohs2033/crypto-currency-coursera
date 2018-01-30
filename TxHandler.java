import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
//import Transaction.Input;
//import Transaction.Output;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	UTXOPool uxp;
    public TxHandler(UTXOPool utxoPool) {
    		// 장부를 만들라고? 어떠헥 장부를 만들라는거지. UTXOPoo컨스트럭터를 이용해서 utxpool의 카피를 만들어야 한다.
    		this.uxp = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,  :done
     * (2) the signatures on each input of {@code tx} are valid,   : done
     * (3) no UTXO is claimed multiple times by {@code tx}, :이거하나만 하면됨!
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool uniqueUtxos = new UTXOPool();
    		byte[] hash = tx.getHash();
        ArrayList<Transaction.Input> inputs = new ArrayList<Transaction.Input>(tx.getInputs());
        ArrayList<Transaction.Output> outputs = new ArrayList<Transaction.Output>(tx.getOutputs());
        int ipSum = 0;
        int opSum = 0;
        
        for (Transaction.Output op : outputs) {
        		int index = outputs.indexOf(op);
        		UTXO tempUt = new UTXO(hash, index);
        		opSum += op.value;
        }
        
        for (Transaction.Input ip : inputs) {
        		UTXO ux = new UTXO(ip.prevTxHash, ip.outputIndex);
        		Transaction.Output tempOp = uxp.getTxOutput(ux);
        		ipSum += tempOp.value;
        		if (!uxp.contains(ux)) return false;
        		//signature.
        		
        		if(!Crypto.verifySignature(tempOp.address, tx.getRawDataToSign(inputs.indexOf(ip)), ip.signature)) return false;
        		if (uniqueUtxos.contains(ux)) return false;
                uniqueUtxos.addUTXO(ux, tempOp);
        }
        
        if(ipSum < opSum) return false;
        
    		return true;
    		// 모든 아웃풋이 현재 UTXO pool에 있는지 체크하는 코드.
    		// tx.outputs을 전부 돌면서?
    		// 각 input의 signature가 valid한지 체크해야한다.
    		// 어떤 UTX도 여러번 선언되면 안된다.
    		//  input value가 out valu의 합이랑 각각 같아야 한다.
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	
    	 Set<Transaction> validTxs = new HashSet<>();

         for (Transaction tx : possibleTxs) {
             if (isValidTx(tx)) {
                 validTxs.add(tx);
                 for (Transaction.Input in : tx.getInputs()) {
                     UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                     uxp.removeUTXO(utxo);
                 }
                 for (int i = 0; i < tx.numOutputs(); i++) {
                     Transaction.Output out = tx.getOutput(i);
                     UTXO utxo = new UTXO(tx.getHash(), i);
                     uxp.addUTXO(utxo, out);
                 }
             }
         }

         Transaction[] validTxArray = new Transaction[validTxs.size()];
         return validTxs.toArray(validTxArray);

    		// possibleTxs들을 인자로 받아서 뭘 한다는걸까?
    		// 각 트랜잭션의 정확도를 체크해서, 상호-valid한 거래들로 수용된 ㅡ랜잭션을 만들어서, UTXO pool을 업데이트 하라는거지?
        // IMPLEMENT THIS
    }

}
