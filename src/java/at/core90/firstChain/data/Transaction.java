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

/**
 *
 * @author Daniel
 */
public class Transaction implements Serializable {

    /**
     * this is also the hash of the transaction
     */
    public String transactionId;

    /**
     * senders address/public key
     */
    public PublicKey sender;

    /**
     * recipients address/public key
     */
    public PublicKey recipient;

    /**
     *
     */
    public double value;

    /**
     * this is to prevent anybody else from spending funds in our wallet
     */
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    /**
     * a rough count of how many transaction have been generated
     */
    private static int sequence = 0;

    /**
     *
     * @param from
     * @param to
     * @param value
     * @param inputs
     */
    public Transaction(PublicKey from, PublicKey to, double value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    @Override
    public String toString() {
        return "Transaction{" + "transactionId=" + transactionId + ", sender=" + StringUtil.getStringFromKey(sender) + ", recipient=" + recipient + ", value=" + value + ", signature=" + signature + ", inputs=" + inputs + ", outputs=" + outputs + '}';
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
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender)
                + StringUtil.getStringFromKey(recipient)
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
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Double.toString(value);
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
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Double.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
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
        for (TransactionInput i : inputs) {
            i.UTXO = FirstChain.getUTXOTotal().get(i.transactionOutputId);
        }

        // check if transaction is valid
        if (getInputsValue() < FirstChain.getMinimumTransaction()) {
            System.out.println("Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // generate transaction outputs:
        double leftOver = getInputsValue() - value; // get the value of inputs then the left over change
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // send the left over 'change' back to sender

        // add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            FirstChain.getUTXOTotal().put(o.getId(), o);
        }

        // remove transaction inputs from UTXO lists as spent
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) {
                continue; // if Transaction can't be found skip it 
            }
            FirstChain.getUTXOTotal().remove(i.UTXO.getId());
        }
        return true;
    }

    /**
     *
     * @return sum of inputs(UTXO) values
     */
    public double getInputsValue() {
        double total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) {
                continue; //if Transaction can't be found skip it 
            }
            total += i.UTXO.getValue();
        }
        return total;
    }

    /**
     *
     * @return sum of outputs
     */
    public double getOutputValue() {
        double total = 0;
        for (TransactionOutput o : outputs) {
            total += o.getValue();
        }
        return total;
    }
}
