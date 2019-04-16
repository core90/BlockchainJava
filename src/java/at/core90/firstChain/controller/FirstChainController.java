/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.controller;

import at.core90.firstChain.data.Block;
import at.core90.firstChain.data.FirstChain;
import at.core90.firstChain.data.Transaction;
import at.core90.firstChain.data.TransactionOutput;
import at.core90.firstChain.data.Wallet;
import java.io.Serializable;
import java.security.Security;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author Daniel
 */
@Named(value = "firstChainController")
@ApplicationScoped
public class FirstChainController implements Serializable {

    Wallet walletA;
    Wallet walletB;

    public FirstChainController() {
    }

    @PostConstruct
    public void init() {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider
        FirstChain.setDifficulty(3);

        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        Wallet.getWalletList().add(walletA);
        Wallet.getWalletList().add(walletB);
        Wallet.getWalletList().add(coinbase);

        // create genesis transaction, which sends 100 FirstCoins to walletA
        FirstChain.setGenesisTransaction(new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100.0, null));
        FirstChain.getGenesisTransaction().generateSignature(coinbase.getPrivateKey()); // manually sign the genesis transaction
        FirstChain.getGenesisTransaction().transactionId = "0"; // manually set the transaction id
        FirstChain.getGenesisTransaction().outputs.add(new TransactionOutput(FirstChain.getGenesisTransaction().recipient, FirstChain.getGenesisTransaction().value, FirstChain.getGenesisTransaction().transactionId)); // manually add the Transaction Output
        FirstChain.getUTXOTotal().put(FirstChain.getGenesisTransaction().outputs.get(0).id, FirstChain.getGenesisTransaction().outputs.get(0)); // its important to store our first transaction in the UTXOTotal list

        System.out.println("Creating and Mining Genesis block...");
        Block genesis = new Block("0");
        genesis.addTransaction(FirstChain.getGenesisTransaction());
        FirstChain.addBlock(genesis);
    }

    //testing
    public void testing() {
        Block previousBlock;
        Block newBlock;

        for (int i = 0; i < 5; i++) {
            System.out.println("trying");
            previousBlock = FirstChain.getBlockchain().get(i);
            newBlock = new Block(previousBlock.getHash());
            newBlock.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 0.5));
            newBlock.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 0.3));
            FirstChain.addBlock(newBlock);
            FirstChain.isChainValid();
        }
        System.out.println(FirstChain.getBlockchain().toString());

    }

    public ArrayList<Block> getBlockchain() {
        return FirstChain.getBlockchain();
    }

    public ArrayList<Wallet> getWalletList() {
        return Wallet.getWalletList();
    }

    public void createWallet() {
        Wallet newWallet = new Wallet();
        Wallet.getWalletList().add(newWallet);
    }
}
