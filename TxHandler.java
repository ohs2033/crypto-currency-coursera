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
    		this.uxp = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,  :done
     * (2) the signatures on each input of {@code tx} are valid,   : done
     * (3) no UTXO is claimed multiple times by {@code tx}, 
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool uniqueUtxos = new UTXOPool();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        
        int ipSum = 0;
        int opSum = 0;
        
        for (Transaction.Output op : outputs) {
        		if (op.value < 0) return false;
        		opSum += op.value;
        }
        
        for (int i = 0; i < tx.numInputs(); i++) {

        		Transaction.Input ip = tx.getInput(i);
        		UTXO ux = new UTXO(ip.prevTxHash, ip.outputIndex);
        		Transaction.Output tempOp = uxp.getTxOutput(ux);
        		ipSum += tempOp.value;
        		if (!uxp.contains(ux)) return false;
        		if(!Crypto.verifySignature(tempOp.address, tx.getRawDataToSign(i), ip.signature)) return false;
        		if (uniqueUtxos.contains(ux)) return false;
                uniqueUtxos.addUTXO(ux, tempOp);
        }
        
        if(ipSum < opSum) return false;
        
    		return true;

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

    }

}
