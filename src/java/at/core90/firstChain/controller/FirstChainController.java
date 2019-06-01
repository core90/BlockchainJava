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
import at.core90.persistence.DatabaseManager;
import static at.core90.persistence.DatabaseManager.saveWallet;
import java.security.Security;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author Daniel
 */
@Named(value = "firstChainController")
@ApplicationScoped
public class FirstChainController implements AutoCloseable {

    static final Logger LOG = Logger.getLogger(FirstChainController.class.getName());

    private String inputPublicKeyRecipient;
    double inputValue;

    Wallet coinbaseWallet;
    Wallet walletA;

    @Inject
    private WalletConroller walletController;

    public FirstChainController() {
    }

    @PostConstruct
    public void init() {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider

        Firstchain.setDifficulty(4);

//         set Genesis Transaction and Wallets for testing
//         create genesis Block
        walletA = new Wallet();
        coinbaseWallet = new Wallet();
        walletA.setPassword("6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b");
        coinbaseWallet.setPassword("6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b");
        saveWallet(walletA);
        saveWallet(coinbaseWallet);

        Firstchain.setGenesisTransaction(new Transaction(coinbaseWallet.getPublicKey(), coinbaseWallet.getPublicKeyString(), walletA.getPublicKeyString(), 100.0, null));

        Firstchain.getGenesisTransaction()
                .generateSignature(coinbaseWallet.getPrivateKey()); // manually sign the genesis transaction
        Firstchain.getGenesisTransaction().setTransactionId("0"); // manually set the transaction id

        Firstchain.getGenesisTransaction().outputs.add(
                new TransactionOutput(
                        Firstchain.getGenesisTransaction().getRecipient(),
                        Firstchain.getGenesisTransaction().getValue(),
                        Firstchain.getGenesisTransaction().getTransactionId())); // manually add the Transaction Output

        Firstchain.getUTXOTotal()
                .put(Firstchain.getGenesisTransaction().outputs.get(0).getIdHashed(),
                        Firstchain.getGenesisTransaction().outputs.get(0)); // its important to store our first transaction in the UTXOTotal list

        System.out.println(
                "Creating and Mining Genesis block...");
        Block genesis = new Block("0");

        genesis.addTransaction(Firstchain.getGenesisTransaction());
        Firstchain.addBlock(genesis);

        DatabaseManager.persist(genesis);
    }

    public void testing() throws Exception {

        Block previousBlock;
        Block newBlock;

        for (int i = 0; i < 5; i++) {
            System.out.println("trying");
            previousBlock = Firstchain.getBlockchain().get(i);
            newBlock = new Block(previousBlock.getHash());
            newBlock.addTransaction(DatabaseManager.walletData().get(0).sendFunds(DatabaseManager.walletData().get(1).getPublicKeyString(), 10));
//            newBlock.addTransaction(walletB.sendFunds(walletA.getPublicKeyString(), 0.3));
            Firstchain.addBlock(newBlock);
            Firstchain.isChainValid();

            DatabaseManager.persist(newBlock);

        }
        System.out.println(Firstchain.getBlockchain().toString());

    }

    public String newTransaction() {
        Block previousBlock;
        Block newBlock;

        previousBlock = Firstchain.getBlockchain().get(Firstchain.getBlockchain().size() - 1);
        newBlock = new Block(previousBlock.getHash());

        if (inputValue < Firstchain.getMinimumTransaction()) {
            return "newTransaction?faces-redirect=true";
        }
        if (!newBlock.addTransaction(walletController.getLoggedInWallet().
                sendFunds(inputPublicKeyRecipient, inputValue))) {
            return "newTransaction?faces-redirect=true";
        }

        Firstchain.addBlock(newBlock);
        Firstchain.isChainValid();

        DatabaseManager.persist(newBlock);
        return "index?faces-redirect=true";
    }

    public List<Block> getBlockData() {
        List<Block> blocks = DatabaseManager.BlockData();
        return blocks;
    }

    public boolean isChainVaild() {
        return Firstchain.isChainValid();
    }

    public List<Block> getBlockchain() {
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
