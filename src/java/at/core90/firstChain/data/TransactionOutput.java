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

    public String id;
    public PublicKey recipient; // also known as the new owner of these coins
    public double value; // the amount of coins they own
    public String parentTransactionId; // the id of the transaction this output was created in

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
}
