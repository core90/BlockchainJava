/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import at.core90.firstChain.helpers.StringUtil;
import at.core90.persistence.DatabaseManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author Daniel
 */
@Entity
public class Block implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String hash;
    private String previousHash;
    private String merkleRoot;
    private long timeStamp; // as number of milliseconds since 1/1/1970
    private Date date;
    private int nonce;

    @OneToMany(mappedBy = "transactionInBlock",
            cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> transactions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "firstchain_id")
    private Firstchain firstchain;

    public Block() {
    }

    // Block Constructor
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime(); // for calculate hash
        this.date = new Date(); // only for output
        this.hash = calculateHash(); // Making sure we do this after we set other values
        //this.blockNumber = blockNumber + 1;
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
//        transaction.setTransactionInBlock(this);
        //DatabaseManager.persist(this);
        //DatabaseManager.persist(transaction);
        System.out.println("Transaction succesfully added to Block");
        return true;
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
        if (!(object instanceof Block)) {
            return false;
        }
        Block other = (Block) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Block{" + "id=" + id + ", hash=" + hash + ", previousHash=" + previousHash + ", merkleRoot=" + merkleRoot + ", timeStamp=" + timeStamp + ", date=" + date + ", nonce=" + nonce + ", transactions=" + transactions + '}';
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Firstchain getFirstchain() {
        return firstchain;
    }

    public void setFirstchain(Firstchain firstchain) {
        this.firstchain = firstchain;
    }

}
