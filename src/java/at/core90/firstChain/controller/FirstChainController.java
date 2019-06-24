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
import java.io.Serializable;
import java.security.Security;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author Daniel
 */
@Named(value = "firstChainController")
@SessionScoped
public class FirstChainController implements AutoCloseable, Serializable {

    static final Logger LOG = Logger.getLogger(FirstChainController.class.getName());

    private String inputPublicKeyRecipient;
    private String existingPublicKey;
    double inputValue;

    Wallet coinbaseWallet;
    Wallet walletA;
    Firstchain firstchain;
    private Block selectedBlock;
    private Transaction selectedTransaction;

    @Inject
    private WalletConroller walletController;

    public FirstChainController() {
    }

    @PostConstruct
    public void init() {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider

        Firstchain.setDifficulty(4);

        doCreateGenesis();
        testing();
    }

    public void doCreateGenesis() {
        //         set Genesis Transaction and Wallets for testing
//         create genesis Block
        walletA = new Wallet();
        coinbaseWallet = new Wallet();
        walletA.setUsername("1");
        walletA.setPassword("6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b");
        coinbaseWallet.setUsername("coinbase");
        coinbaseWallet.setPassword("6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b");
        saveWallet(walletA);
        saveWallet(coinbaseWallet);

        Firstchain.setGenesisTransaction(new Transaction(
                coinbaseWallet.getPublicKey(),
                coinbaseWallet.getPublicKeyString(),
                walletA.getPublicKeyString(), 100.0, null));

        Firstchain.getGenesisTransaction()
                .generateSignature(coinbaseWallet.getPrivateKey()); // manually sign the genesis transaction
        Firstchain.getGenesisTransaction().setTransactionHash("0"); // manually set the transaction id

        Firstchain.getGenesisTransaction().outputs.add(
                new TransactionOutput(
                        Firstchain.getGenesisTransaction().getRecipient(),
                        Firstchain.getGenesisTransaction().getValue(),
                        Firstchain.getGenesisTransaction().getTransactionHash())); // manually add the Transaction Output

        Firstchain.getUTXOTotal().put(
                Firstchain.getGenesisTransaction().outputs.get(0).getIdHashed(),
                Firstchain.getGenesisTransaction().outputs.get(0)); // its important to store our first transaction in the UTXOTotal list

        System.out.println(
                "Creating and Mining Genesis block...");
        Block genesis = new Block("0");

        genesis.addTransaction(Firstchain.getGenesisTransaction());
        Firstchain.addBlock(genesis);
        //genesis.setFirstchain(firstchain);
        DatabaseManager.persist(genesis);
    }

    public void testing() {

        Block previousBlock;
        Block newBlock;

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String showBlock(Block block) {
        selectedBlock = block;
        return "showSingleRow?faces-redirect=true";
    }

    public String newTransaction() {
        Block previousBlock;
        Block newBlock;

        //List<Block> blocks = DatabaseManager.blockData();
        previousBlock = Firstchain.getBlockchain().get(Firstchain.getBlockchain().size() - 1);
        //previousBlock = blocks.get(blocks.size() - 1);
        System.out.println("Previous Block: " + previousBlock);
        newBlock = new Block(previousBlock.getHash());

        if (inputValue < Firstchain.getMinimumTransaction()) {
            FacesMessage invalidUserPw = new FacesMessage("Minimum Value 0.1 Firstcoins");
            FacesContext.getCurrentInstance().addMessage("newTransactionForm:sendButton", invalidUserPw);
            return null;
        }

        existingPublicKey = DatabaseManager.queryDoPublicKeyExist(inputPublicKeyRecipient);
        if (existingPublicKey == null) {
            FacesMessage invalidPublicKey = new FacesMessage("Public Key not found");
            FacesContext.getCurrentInstance().addMessage("newTransactionForm:sendButton", invalidPublicKey);
            return null;
        }
        if (!newBlock.addTransaction(walletController.getLoggedInWallet().
                sendFunds(inputPublicKeyRecipient, inputValue))) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("param1", "Transaction failed!");
            return "newTransaction?faces-redirect=true";
        }

        Firstchain.addBlock(newBlock);
        Firstchain.isChainValid();

        DatabaseManager.persist(newBlock);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("param1", "Transaction succesful!");
        return "walletUser?faces-redirect=true";
    }

    public Transaction getSingleTransaction() {
        int i = selectedBlock.getId().intValue();
        selectedTransaction = Firstchain.getBlockchain().get(i-1).getTransactions().get(0);
        return selectedTransaction;
    }
    
    public List<Block> getBlockData() {
        List<Block> blocks = DatabaseManager.blockData();
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

    public Block getSelectedBlock() {
        return selectedBlock;
    }

    public void setSelectedBlock(Block selectedBlock) {
        this.selectedBlock = selectedBlock;
    }

    public Transaction getSelectedTransaction() {
        return selectedTransaction;
    }

    public void setSelectedTransaction(Transaction selectedTransaction) {
        this.selectedTransaction = selectedTransaction;
    }
}
