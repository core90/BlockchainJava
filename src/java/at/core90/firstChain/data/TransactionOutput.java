/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import at.core90.firstChain.helpers.StringUtil;
import java.io.Serializable;
import java.security.PublicKey;

/**
 *
 * @author Daniel
 */
public class TransactionOutput implements Serializable {

    private String id;
    private PublicKey recipient; // also known as the new owner of these coins
    private double value; // the amount of coins they own
    private String parentTransactionId; // the id of the transaction this output was created in

    // Contructor
    public TransactionOutput(PublicKey recipient, double value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + Double.toString(value) + parentTransactionId);
    }

    // Check if coin belongs to you
    /**
     *
     * @param publicKey
     * @return true if coin belong to this wallet
     */
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }

    @Override
    public String toString() {
        return "TransactionOutput{" + "id=" + id + ", recipient=" + recipient + ", value=" + value + ", parentTransactionId=" + parentTransactionId + '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public void setRecipient(PublicKey recipient) {
        this.recipient = recipient;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }
}
