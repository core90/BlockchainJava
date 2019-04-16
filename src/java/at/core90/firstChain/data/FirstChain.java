package at.core90.firstChain.data;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.security.Security;
import java.util.HashMap;

/**
 *
 * @author Daniel
 */
public class FirstChain implements Serializable {

    private static ArrayList<Block> blockchain = new ArrayList<Block>();

    /**
     * List of all unspent transactions
     */
    private static HashMap<String, TransactionOutput> UTXOTotal = new HashMap<String, TransactionOutput>();

    /**
     * Set the difficulty for mining
     */
    private static int difficulty;

    private static double minimumTransaction = 0.1;
    private static Transaction genesisTransaction;

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        /**
         * a temporary working list of unspent transactions at a given block
         * state
         */
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();

        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        // loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            // check if hash is solved
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }

            // loop through blockchains transactions
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                // check if current transaction can be verified
                if (!currentTransaction.verifySignature()) {
                    System.out.println("Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }

                // check if in- and output values are equal
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputValue()) {
                    System.out.println("Inputs are not equal to outputs on Transaction(" + t + ") is Invalid");
                    return false;
                }

                // loop through transaction inputs
                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    // check if input is referenced
                    if (tempOutput == null) {
                        System.out.println("Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    // check if input value == output value
                    if (input.UTXO.value != tempOutput.value) {
                        System.out.println("Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                // loop through transaction outputs
                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                // check if recipient is valid
                if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    System.out.println("Transaction(" + t + ") output recipient is not who it should  be");
                    return false;
                }

                // check if output 'change' belongs to sender
                if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    System.out.println("Transaction(" + t + ") output 'change' is not sender");
                    return false;
                }
            }
        }
        // everything went fine
        System.out.println("Blockchain is Valid");
        return true;
    }

    /**
     * adds a new block to the chain
     *
     * @param newBlock
     */
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public static ArrayList<Block> getBlockchain() {
        return blockchain;
    }

    public static void setBlockchain(ArrayList<Block> aBlockchain) {
        blockchain = aBlockchain;
    }

    public static HashMap<String, TransactionOutput> getUTXOTotal() {
        return UTXOTotal;
    }

    public static void setUTXOTotal(HashMap<String, TransactionOutput> aUTXOTotal) {
        UTXOTotal = aUTXOTotal;
    }

    public static int getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(int aDifficulty) {
        difficulty = aDifficulty;
    }

    public static double getMinimumTransaction() {
        return minimumTransaction;
    }

    public static void setMinimumTransaction(double aMinimumTransaction) {
        minimumTransaction = aMinimumTransaction;
    }

    public static Transaction getGenesisTransaction() {
        return genesisTransaction;
    }

    public static void setGenesisTransaction(Transaction aGenesisTransaction) {
        genesisTransaction = aGenesisTransaction;
    }

}
