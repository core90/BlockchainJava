/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.data;

import at.core90.firstChain.helpers.StringUtil;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 *
 * @author Daniel
 */
@Entity
public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 2048)
    private PrivateKey privateKey;
    
    @Column(length = 2048)
    private PublicKey publicKey;

    @Column(length = 2048)
    private String privateKeyString;

    @Column(length = 2048)
    private String publicKeyString;

    private String password;

    @OneToMany(mappedBy = "transactionOutputsWallet", cascade = CascadeType.ALL, fetch = FetchType.EAGER) // OneToMany OK?????????????
    //@MapKey(name = "idHashed") // richtiger mapping key????????????????
    private Map<String, TransactionOutput> UTXOWallet = new HashMap<String, TransactionOutput>();

    //private static List<Wallet> walletList = new ArrayList<Wallet>();

    @Transient
    Firstchain firstchain = new Firstchain();
    
    public Wallet() {
        this.password = password;
        generateKeyPair();
    }

    /**
     * Uses Elliptic-curve cryptography to generate a keypair (Private and
     * Public Key)
     */
    public void generateKeyPair() {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider

        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC"); //  Elliptic Curve Digital Signature Algorithm , Bouncy Castle (Provider)
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); //SHA1 = 160-bit (20-byte)-Hash based on a Pseodo Random Number Generator
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1"); //Curve prime192v1 (192 bits)

            // initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable security level
            KeyPair keyPair = keyGen.generateKeyPair();

            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            privateKeyString = StringUtil.getStringFromKey(privateKey);
            publicKeyString = StringUtil.getStringFromKey(publicKey);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return balance and stores the UTXO`s owned by this wallet in
     * this.UTXOTotal
     */
    public double getBalance() {
        double total = 0;
        for (Map.Entry<String, TransactionOutput> item : firstchain.getUTXOTotal().entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKeyString)) { // if output belongs to this wallet (if coins belong to this wallet)
                UTXOWallet.put(UTXO.getIdHashed(), UTXO); // add it to our list of unspent transactions
                total += UTXO.getValue(); // add owned coins to the wallet
            }
        }
        return total;
    }

    /**
     * Checks if wallet has enough funds, <br>
     * get UTXO`s value until amount is reached <br>
     * remove spent output id`s <br>
     * apply signature to Transaction <br>
     *
     * @see Transaction.generateSignature
     * @param recipient
     * @param value
     * @return a new transaction
     */
    public Transaction sendFunds(String recipient, double value) {

        // gather balance and check funds 
        if (getBalance() < value) {
            System.out.println("Not enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        // create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        double total = 0;
        // iterate over outputs in UTXO of this wallet 
        for (Map.Entry<String, TransactionOutput> item : UTXOWallet.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();

            // store the id`s of the outputs in inputs array
            inputs.add(new TransactionInput(UTXO.getIdHashed()));
            if (total > value) {
                break;
            }
        }

        // create new Transaction
        Transaction newTransaction = new Transaction(publicKey, publicKeyString, recipient, value, inputs);
        // generate signature for new Transaction
        newTransaction.generateSignature(privateKey);

        // iterate over input array and remove spent outputs
        for (TransactionInput input : inputs) {
            UTXOWallet.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }

    @Override
    public String toString() {
        return "Wallet{" + "id=" + id + ", privateKey=" + privateKey + ", publicKey=" + publicKey + ", privateKeyString=" + privateKeyString + ", publicKeyString=" + publicKeyString + ", password=" + password + ", UTXOWallet=" + UTXOWallet + '}';
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKeyString() {
        return privateKeyString;
    }

    public void setPrivateKeyString(String privateKeyString) {
        this.privateKeyString = privateKeyString;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public void setPublicKeyString(String publicKeyString) {
        this.publicKeyString = publicKeyString;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, TransactionOutput> getUTXOWallet() {
        return UTXOWallet;
    }

    public void setUTXOWallet(HashMap<String, TransactionOutput> UTXOWallet) {
        this.UTXOWallet = UTXOWallet;
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
        if (!(object instanceof Wallet)) {
            return false;
        }
        Wallet other = (Wallet) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

//    public static List<Wallet> getWalletList() {
//        return walletList;
//    }
//
//    public static void setWalletList(ArrayList<Wallet> aWalletList) {
//        walletList = aWalletList;
//    }
}
