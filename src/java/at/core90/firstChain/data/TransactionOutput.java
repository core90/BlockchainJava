/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import at.core90.firstChain.helpers.StringUtil;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Daniel
 */
@Entity
public class TransactionOutput implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idAuto;

    private String idHashed;
    private String recipient; // also known as the new owner of these coins
    private double value; // the amount of coins they own
    private String parentTransactionId; // the idHashed of the transaction this output was created in

    @ManyToOne
    @JoinColumn
    private Firstchain utxoTotal;

    @ManyToOne
    @JoinColumn
    private Transaction transactionOutput;

    @ManyToOne
    @JoinColumn
    private Wallet transactionOutputsWallet;

    public TransactionOutput() {
    }

    // Contructor
    public TransactionOutput(String recipient, double value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.idHashed = StringUtil.applySha256(recipient + Double.toString(value) + parentTransactionId);
    }

    // Check if coin belongs to you
    /**
     *
     * @param publicKey
     * @return true if coin belong to this wallet
     */
    public boolean isMine(String publicKey) {
        return (publicKey.equals(recipient));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idHashed != null ? idHashed.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the idHashed fields are not set
        if (!(object instanceof TransactionOutput)) {
            return false;
        }
        TransactionOutput other = (TransactionOutput) object;
        if ((this.idHashed == null && other.idHashed != null) || (this.idHashed != null && !this.idHashed.equals(other.idHashed))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TransactionOutput{" + "idAuto=" + idAuto + ", id=" + idHashed + ", recipient=" + recipient + ", value=" + value + ", parentTransactionId=" + parentTransactionId + ", transactionOutputTotal=" + utxoTotal + ", transactionOutput=" + transactionOutput + ", transactionOutputsWallet=" + transactionOutputsWallet + '}';
    }

    public Long getIdAuto() {
        return idAuto;
    }

    public void setIdAuto(Long idAuto) {
        this.idAuto = idAuto;
    }

    public String getIdHashed() {
        return idHashed;
    }

    public void setIdHashed(String idHashed) {
        this.idHashed = idHashed;
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

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

}
