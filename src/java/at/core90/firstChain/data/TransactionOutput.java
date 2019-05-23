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

    private String id;
    private String recipient; // also known as the new owner of these coins
    private double value; // the amount of coins they own
    private String parentTransactionId; // the id of the transaction this output was created in

    public TransactionOutput() {
    }

    // Contructor
    public TransactionOutput(String recipient, double value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(recipient + Double.toString(value) + parentTransactionId);
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TransactionOutput)) {
            return false;
        }
        TransactionOutput other = (TransactionOutput) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "at.core90.firstChain.persistence.TransactionOutput[ id=" + id + " ]";
    }

    public Long getIdAuto() {
        return idAuto;
    }

    public void setIdAuto(Long idAuto) {
        this.idAuto = idAuto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
