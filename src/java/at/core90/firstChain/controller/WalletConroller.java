/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.firstChain.controller;

import at.core90.firstChain.data.Wallet;
import at.core90.firstChain.helpers.CryptException;
import at.core90.firstChain.helpers.JSFUtil;
import at.core90.firstChain.helpers.StringUtil;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel
 */
@Named(value = "walletConroller")
@SessionScoped
public class WalletConroller implements Serializable {

    static final Logger LOG = Logger.getLogger(WalletConroller.class.getName());

    private Wallet loggedInWallet;
    private String inputPublicKey;
    private String inputPassword;

    /**
     * Creates a new instance of WalletConroller
     */
    public WalletConroller() {
    }

    public void init() {
    }

    public String createWallet() {

        Wallet newWallet = new Wallet();
        try {
            newWallet.setPassword(StringUtil.getSha256(inputPassword));
        } catch (CryptException ex) {
            Logger.getLogger(WalletConroller.class.getName()).log(Level.SEVERE, null, ex);
        }
        Wallet.getWalletList().add(newWallet);
        loggedInWallet = newWallet;
        return "walletUser?redirect-true";
    }

    public String doLogin() {
        Wallet tmpWallet = null;

        for (Wallet wallet : Wallet.getWalletList()) {

            try {
                if (StringUtil.getStringFromKey(wallet.getPublicKey()).equals(inputPublicKey)
                        && wallet.getPassword().equals(StringUtil.getSha256(inputPassword))) {
                    tmpWallet = wallet;
                    break;
                }
            } catch (CryptException ce) {
                LOG.warning("Cannot calculate SHA256" + ce.getMessage());
            }
        }

        if (Objects.nonNull(tmpWallet)) {
            loggedInWallet = tmpWallet;
            return "walletUser?faces-redirect=true";
        }

        JSFUtil.displayWarning("Invalid Public Key or Password!");
        return null;
    }

    @Override
    public String toString() {
        return "WalletConroller{" + "loggedInWallet=" + loggedInWallet + ", inputPublicKey=" + inputPublicKey + ", inputPassword=" + inputPassword + '}';
    }

    public ArrayList<Wallet> getWalletList() {
        return Wallet.getWalletList();
    }

    public Wallet getLoggedInWallet() {
        return loggedInWallet;
    }

    public void setLoggedInWallet(Wallet loggedInWallet) {
        this.loggedInWallet = loggedInWallet;
    }

    public String getInputPublicKey() {
        return inputPublicKey;
    }

    public void setInputPublicKey(String inputPublicKey) {
        this.inputPublicKey = inputPublicKey;
    }

    public String getInputPassword() {
        return inputPassword;
    }

    public void setInputPassword(String inputPassword) {
        this.inputPassword = inputPassword;
    }
}
