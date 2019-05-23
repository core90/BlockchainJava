/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.controller;

import at.core90.firstChain.data.Block;
import at.core90.firstChain.data.Firstchain;
import at.core90.firstChain.data.Transaction;
import at.core90.firstChain.data.TransactionOutput;
import at.core90.firstChain.data.Wallet;
import java.io.Serializable;
import java.security.Security;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Daniel
 */
@Named(value = "firstChainController")
@ApplicationScoped
public class FirstChainController implements AutoCloseable {

    static final Logger LOG = Logger.getLogger(FirstChainController.class.getName());

//    private EntityManagerFactory emf;
//    private EntityManager em;

    private String inputPublicKeyRecipient;
    double inputValue;

    Wallet walletA;
    Wallet walletB;

    @Inject
    private WalletConroller walletController;

    public FirstChainController() {
    }

    @PostConstruct
    public void init() {

//        emf = Persistence.createEntityManagerFactory("FirstChainWebPU");
//        em = emf.createEntityManager();
//
//        LOG.info("Init DB done.");
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider

        Firstchain.setDifficulty(3);

        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        Wallet.getWalletList()
                .add(walletA);
        Wallet.getWalletList()
                .add(walletB);
        Wallet.getWalletList()
                .add(coinbase);

        // create genesis transaction, which sends 100 FirstCoins to walletA
        Firstchain.setGenesisTransaction(
                new Transaction(coinbase.getPublicKey(), coinbase.getPublicKeyString(), walletA.getPublicKeyString(), 100.0, null));
        Firstchain.getGenesisTransaction()
                .generateSignature(coinbase.getPrivateKey()); // manually sign the genesis transaction
        Firstchain.getGenesisTransaction().setTransactionId("0"); // manually set the transaction id

        Firstchain.getGenesisTransaction().outputs.add(
                new TransactionOutput(
                        Firstchain.getGenesisTransaction().getRecipient(),
                        Firstchain.getGenesisTransaction().getValue(),
                        Firstchain.getGenesisTransaction().getTransactionId())); // manually add the Transaction Output

        Firstchain.getUTXOTotal()
                .put(Firstchain.getGenesisTransaction().outputs.get(0).getId(),
                        Firstchain.getGenesisTransaction().outputs.get(0)); // its important to store our first transaction in the UTXOTotal list

        System.out.println(
                "Creating and Mining Genesis block...");
        Block genesis = new Block("0");

        genesis.addTransaction(Firstchain.getGenesisTransaction());
        Firstchain.addBlock(genesis);
    }
    //testing

    public void testing() {
        Block previousBlock;
        Block newBlock;

        for (int i = 0; i < 5; i++) {
            System.out.println("trying");
            previousBlock = Firstchain.getBlockchain().get(i);
            newBlock = new Block(previousBlock.getHash());
            newBlock.addTransaction(walletA.sendFunds(walletB.getPublicKeyString(), 0.5));
            newBlock.addTransaction(walletB.sendFunds(walletA.getPublicKeyString(), 0.3));
            Firstchain.addBlock(newBlock);
            Firstchain.isChainValid();
        }
        System.out.println(Firstchain.getBlockchain().toString());

    }

    public void newTransaction() {
        Block previousBlock;
        Block newBlock;

        previousBlock = Firstchain.getBlockchain().get(Firstchain.getBlockchain().size() - 1);
        newBlock = new Block(previousBlock.getHash());

        newBlock.addTransaction(walletController.getLoggedInWallet().sendFunds(inputPublicKeyRecipient, inputValue));
        Firstchain.addBlock(newBlock);
        Firstchain.isChainValid();
    }

    public boolean isChainVaild() {
        return Firstchain.isChainValid();
    }

    public ArrayList<Block> getBlockchain() {
        return Firstchain.getBlockchain();
    }

    public String getInputPublicKeyRecipient() {
        return inputPublicKeyRecipient;
    }

    public void setInputPublicKeyRecipient(String inputPublicKeyRecipient) {
        this.inputPublicKeyRecipient = inputPublicKeyRecipient;
    }

    public double getInputValue() {
        return inputValue;
    }

    public void setInputValue(double inputValue) {
        this.inputValue = inputValue;
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
