/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import at.core90.firstChain.helpers.StringUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Daniel
 */
public class Block implements Serializable {

    private String hash;
    private String previousHash;
    private String merkleRoot;
    private ArrayList<Transaction> transactions = new ArrayList<>(); // our data will be a simple message
    private long timeStamp; // as number of milliseconds since 1/1/1970
    private Date date;
    private int nonce;
    //private int blockNumber;

    // Block Constructor
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime(); // for calculate hash
        this.date = new Date(); // only for output
        this.hash = calculateHash(); // Making sure we do this after we set other values
        //this.blockNumber = blockNumber + 1;
    }

    @Override
    public String toString() {
        return "Block{" + "hash=" + hash + ", previousHash=" + previousHash + ", transactions=" + transactions + ", nonce=" + nonce + '}';
    }

    /**
     * Calculate new hash based on blocks contents
     *
     * @return calculated hash
     */
    public String calculateHash() {
        String calculatehash = StringUtil.applySha256(
                previousHash + Long.toString(timeStamp)
                + Integer.toString(nonce) + merkleRoot);
        return calculatehash;
    }

    /**
     * increase nonce value until hash target is reached
     *
     * @param difficulty
     */
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty); // Create a string with difficulty * "0" 
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("nonce: " + nonce);
        System.out.println("Block Mined!!! : " + hash);
    }

    /**
     * Add transactions to this block
     *
     * @param transaction
     * @return true if transaction is valid
     */
    public boolean addTransaction(Transaction transaction) {
        // process transaction and check if valid, unless block is genesis block then ignore
        if (transaction == null) {
            return false;
        }

        if ((!"0".equals(previousHash))) {
            if (transaction.processTransaction() != true) {
                System.out.println("Transaction failed to process. Discarded");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction succesfully added to Block");
        return true;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

//    public int getBlockNumber() {
//        return blockNumber;
//    }
//
//    public void setBlockNumber(int blockNumber) {
//        this.blockNumber = blockNumber;
//    }
}
