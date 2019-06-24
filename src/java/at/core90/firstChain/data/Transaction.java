/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import at.core90.firstChain.helpers.StringUtil;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * this is also the hash of the transaction
     */
    private String transactionHash;

    @Column(length = 2048)
    private PublicKey pubKeySender;

    /**
     * senders address/public key
     */
    private String sender;

    /**
     * recipients address/public key
     */
    private String recipient;

    /**
     *
     */
    private double value;

    /**
     * this is to prevent anybody else from spending funds in our wallet
     */
    private byte[] signature;

    @OneToMany(mappedBy = "transactionInput",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private List<TransactionInput> inputs = new ArrayList<>();

    @OneToMany(mappedBy = "transactionOutput",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    public List<TransactionOutput> outputs = new ArrayList<>();

    /**
     * a rough count of how many transaction have been generated
     */
    private static int sequence = 0;

    @ManyToOne
    @JoinColumn(name = "block_id")
    private Block transactionInBlock;

    public Transaction() {
    }

    /**
     *
     * @param from
     * @param to
     * @param value
     * @param inputs
     */
    public Transaction(PublicKey pubKeySender, String from, String to, double value, ArrayList<TransactionInput> inputs) {
        this.pubKeySender = pubKeySender;
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    /**
     * this calculates the transactions hash (which will be used as its Id)
     *
     * @return String calculated Hash from Public Key of sender and recipient
     * and applies sha256, adding sequence to avoid two identical transactions
     * having the same hash
     * @see StringUtil#applySha256(java.lang.String)
     */
    private String calculateHash() {
        sequence++; // increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(sender + recipient
                + Double.toString(value) + sequence
        );
    }

    /**
     * Signs all the data we dont wish to be tampered with Applies ECDASig on
     * private key and the data (Public Key of sender and recipient + value of
     * transaction)
     *
     * @param privateKey
     * @see StringUtil#applyECDSASig(java.security.PrivateKey, java.lang.String)
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = sender + recipient + Double.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    /**
     * Verifies the signed data using StringUtil.verifyECDSASig
     *
     * @see StringUtil#verifyECDSASig(java.security.PublicKey, java.lang.String,
     * byte[])
     * @return true if signature can be verified
     */
    public boolean verifySignature() {
        String data = sender + recipient + Double.toString(value);
        return StringUtil.verifyECDSASig(pubKeySender, data, signature);
    }

    /**
     * Checks if
     * <p>
     * - Signature can be verified</p>
     * <p>
     * - Transaction value is bigger than minimum transaction value</p>
     *
     * <p>
     * Sends the value to the recipient and adds the outputs to the UTXO list
     * </p>
     *
     * Removes transaction inputs UTXO lists as spent
     *
     * @see Transaction#verifySignature()
     * @see Transaction#getInputsValue()
     *
     * @return true if new transaction could be created
     */
    public boolean processTransaction() {

        if (verifySignature() == false) {
            System.out.println("Transaction Signature failed to verify");
            return false;
        }

        // gather transaction inputs (Make sure they are unspent)
        //List<TransactionInput> inputs = DatabaseManager.transactionInputData();
        for (TransactionInput i : inputs) {
            i.setUTXO(Firstchain.getUTXOTotal().get(i.getTransactionOutputId()));
        }

        // check if transaction is valid
        if (getInputsValue() < Firstchain.getMinimumTransaction()) {
            System.out.println("Transaction Inputs to small: " + getInputsValue());
            System.out.println("Please enter amount greater than: " + Firstchain.getMinimumTransaction());
            return false;
        }

        // generate transaction outputs:
        double leftOver = getInputsValue() - value; // get the value of inputs then the left over change
        transactionHash = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionHash)); // send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionHash)); // send the left over 'change' back to sender

        // add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            Firstchain.getUTXOTotal().put(o.getIdHashed(), o);
        }

        // remove transaction inputs from UTXO lists as spent
        for (TransactionInput i : inputs) {
            if (i.getUTXO() == null) {
                continue; // if Transaction can't be found skip it 
            }
            Firstchain.getUTXOTotal().remove(i.getUTXO().getIdHashed());
        }
        return true;
    }

    /**
     *
     * @return sum of inputs(UTXO) values
     */
    public double getInputsValue() {
        double total = 0;
        //inputs = DatabaseManager.transactionInputData();
        for (TransactionInput i : inputs) {
            if (i.getUTXO() == null) {
                continue; //if Transaction can't be found skip it 
            }
            total += i.getUTXO().getValue();
        }
        return total;
    }

    /**
     *
     * @return sum of outputs
     */
    public double getOutputValue() {
        double total = 0;
        //outputs = DatabaseManager.transactionOutputData();
        for (TransactionOutput o : outputs) {
            total += o.getValue();
        }
        return total;
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
        if (!(object instanceof Transaction)) {
            return false;
        }
        Transaction other = (Transaction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "" + "Transaction Hash: " + transactionHash + ", " + "Sender: " + sender + ", " + "Recipient: " + recipient + ", Value: " + value + ", Signature: " + signature;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    public static int getSequence() {
        return sequence;
    }

    public static void setSequence(int aSequence) {
        sequence = aSequence;
    }

    public PublicKey getPubKeySender() {
        return pubKeySender;
    }

    public void setPubKeySender(PublicKey pubKeySender) {
        this.pubKeySender = pubKeySender;
    }

    public Block getTransactionInBlock() {
        return transactionInBlock;
    }

    public void setTransactionInBlock(Block transactionInBlock) {
        this.transactionInBlock = transactionInBlock;
    }

}
