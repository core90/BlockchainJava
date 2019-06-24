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
import at.core90.persistence.DatabaseManager;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

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
    private String inputUsername;
    private String inputPassword;
    private String inputPassword2;

    /**
     * Creates a new instance of WalletConroller
     */
    public WalletConroller() {
    }

    public void init() {
    }

    public String createWallet() {

        Wallet newWallet = new Wallet();
        String existingUser;

        if (!inputPassword.equals(inputPassword2) ) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("param2", "Passwords are not the same!");
            return null;
        }

        try {
            newWallet.setPassword(StringUtil.getSha256(inputPassword));
        } catch (CryptException ex) {
            Logger.getLogger(WalletConroller.class.getName()).log(Level.SEVERE, null, ex);
        }

        newWallet.setUsername(inputUsername);

        existingUser = DatabaseManager.queryDoUserExist(inputUsername);

        if (existingUser == null) {
            loggedInWallet = newWallet;

            DatabaseManager.saveWallet(newWallet);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("param2", "New Wallet created!");
            return "walletUser?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("param2", "Username already exists!");
            return "createNewWallet?faces-redirect=true";
        }

    }

    public String doLogin() {

        String inputPasswordHashed = null;

        try {
            inputPasswordHashed = StringUtil.getSha256(inputPassword);

        } catch (CryptException ex) {
            Logger.getLogger(WalletConroller.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        loggedInWallet = DatabaseManager.queryWalletUser(inputUsername, inputPasswordHashed);
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX:     " + loggedInWallet);
        if (loggedInWallet == null) {
            JSFUtil.displayWarning("Invalid Username and/or Password");
            FacesMessage invalidUserPw = new FacesMessage("Invalid Username and/or Password");
            FacesContext.getCurrentInstance().addMessage("loginForm:loginButton", invalidUserPw);

            return null;
        } else {
            return "walletUser?faces-redirect=true";
        }

    }

    public String doLogout() {
        loggedInWallet = null;
        return "index?faces-redirect=true";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.loggedInWallet);
        hash = 59 * hash + Objects.hashCode(this.inputPublicKey);
        hash = 59 * hash + Objects.hashCode(this.inputPassword);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WalletConroller other = (WalletConroller) obj;
        if (!Objects.equals(this.inputPublicKey, other.inputPublicKey)) {
            return false;
        }
        if (!Objects.equals(this.inputPassword, other.inputPassword)) {
            return false;
        }
        if (!Objects.equals(this.loggedInWallet, other.loggedInWallet)) {
            return false;
        }
        return true;
    }

    public List<Wallet> getWalletData() {
        List<Wallet> wallets = DatabaseManager.walletData();
        return wallets;
    }

    @Override
    public String toString() {
        return "WalletConroller{" + "loggedInWallet=" + loggedInWallet + ", inputPublicKey=" + inputPublicKey + ", inputPassword=" + inputPassword + '}';
    }

//    public List<Wallet> getWalletList() {
//        return Wallet.getWalletList();
//    }
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

    public String getInputUsername() {
        return inputUsername;
    }

    public void setInputUsername(String inputUsername) {
        this.inputUsername = inputUsername;
    }

    public String getInputPassword2() {
        return inputPassword2;
    }

    public void setInputPassword2(String inputPassword2) {
        this.inputPassword2 = inputPassword2;
    }

}
