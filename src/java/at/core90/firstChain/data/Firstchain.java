/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 *
 * @author Daniel
 */
@Entity
public class Firstchain implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "firstchain",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private static List<Block> blockchain = new ArrayList<Block>();

    /**
     * List of all unspent transactions
     */
    @OneToOne(mappedBy = "utxoTotal",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @MapKey(name = "idHashed")
    private static Map<String, TransactionOutput> UTXOTotal = new HashMap<String, TransactionOutput>();

    /**
     * Set the difficulty for mining
     */
    private static int difficulty;

    private static double minimumTransaction = 0.1;
    private static Transaction genesisTransaction;

    public Firstchain() {
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        /**
         * a temporary working list of unspent transactions at a given block
         * state
         */
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();

        tempUTXOs.put(genesisTransaction.outputs.get(0).getIdHashed(), genesisTransaction.outputs.get(0));

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
                for (TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    // check if input is referenced
                    if (tempOutput == null) {
                        System.out.println("Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    // check if input value == output value
                    if (input.getUTXO().getValue() != tempOutput.getValue()) {
                        System.out.println("Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                // loop through transaction outputs
                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.getIdHashed(), output);
                }

                // check if recipient is valid
                if (currentTransaction.outputs.get(0).getRecipient() != currentTransaction.getRecipient()) {
                    System.out.println("Transaction(" + t + ") output recipient is not who it should  be");
                    return false;
                }

                // check if output 'change' belongs to sender
                if (currentTransaction.outputs.get(1).getRecipient() != currentTransaction.getSender()) {
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
//        Firstchain firstchain = new Firstchain();
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
//        newBlock.setFirstchain(firstchain);
        //DatabaseManager.persist(firstchain);
        //DatabaseManager.persist(newBlock);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Firstchain)) {
            return false;
        }
        Firstchain other = (Firstchain) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "at.core90.firstChain.persistence.Firstchain[ id=" + id + " ]";
    }

    public static List<Block> getBlockchain() {
        return blockchain;
    }

    public static void setBlockchain(ArrayList<Block> aBlockchain) {
        blockchain = aBlockchain;
    }

    public static Map<String, TransactionOutput> getUTXOTotal() {
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
