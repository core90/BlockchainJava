/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.core90.persistence;

import at.core90.firstChain.data.Block;
import at.core90.firstChain.data.Firstchain;
import at.core90.firstChain.data.Transaction;
import at.core90.firstChain.data.TransactionOutput;
import at.core90.firstChain.data.Wallet;
import static com.sun.faces.util.CollectionsUtils.map;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import static jdk.nashorn.internal.objects.NativeArray.map;

/**
 *
 * @author Daniel
 */
public class DatabaseManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("FirstChainWebPU");
    private static EntityManager em;
    static Wallet walletA;
    static Wallet coinbaseWallet;

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public DatabaseManager() {
        LOG.info("Before init DB!!!");
        emf = Persistence.createEntityManagerFactory("FirstChainWebPU");
        LOG.info("emf done");
        em = emf.createEntityManager();
        LOG.info("Init DB done");
    }

    public static void saveWallet(Wallet wallet) {

        em = getEntityManager();

        em.getTransaction().begin();
        LOG.info("STARTING TRANSACTION");
        try {
            if (Objects.nonNull(wallet.getId())) {
                wallet = em.merge(wallet);
                LOG.info("WALLET MERGED");
            }
            em.persist(wallet);
            em.getTransaction().commit();
            LOG.info("WALLET SAVED");
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
        em.close();
    }

    public static void readOut() {

        em = getEntityManager();

        em.getTransaction().begin();
        List<Wallet> wallets = em.createQuery("SELECT w FROM Wallet w", Wallet.class).getResultList();
        em.getTransaction().commit();
        for (Wallet wallet : wallets) {
            LOG.info("Wallet mit id " + wallet.getId() + " PubKey " + wallet.getPublicKeyString() + " Private Key" + wallet.getPrivateKeyString());
        }
    }

    public static Wallet queryWalletUser(String inputPubKey, String inputPasswordHashed) {

        em = getEntityManager();
        Wallet loggedInWallet = null;

        em.getTransaction().begin();

        try {
            TypedQuery<Wallet> query = em.createQuery("SELECT w FROM Wallet w "
                    + "WHERE w.publicKeyString = :publicKeyString "
                    + "AND w.password = :password", Wallet.class);
            loggedInWallet = query.setParameter(
                    "publicKeyString", inputPubKey).setParameter("password", inputPasswordHashed)
                    .getSingleResult();
        } catch (Exception e) {
                e.printStackTrace();
        }
        em.getTransaction().commit();
        em.close();

        return loggedInWallet;
    }

    public static List<Wallet> walletData() {
        em = getEntityManager();

        em.getTransaction().begin();
        List<Wallet> wallets = em.createQuery("SELECT w FROM Wallet w", Wallet.class).getResultList();
        em.getTransaction().commit();
        em.close();

        return wallets;
    }

    public static List<Block> BlockData() {
        em = getEntityManager();

        em.getTransaction().begin();
        List<Block> blocks = em.createQuery("SELECT b FROM Block b", Block.class).getResultList();
        em.getTransaction().commit();
        em.close();

        return blocks;
    }

    public static void persist(Object object) {
        em = getEntityManager();

        em.getTransaction().begin();
        LOG.info("STARTING TRANSACTION: " + object.getClass().toString());
        try {
            em.persist(object);
            em.getTransaction().commit();
            LOG.info("SAVED: " + object.getClass().toString());
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }

        em.close();
    }

    @Override
    public void close() throws Exception {
        em.close();
        emf.close();
    }

}
