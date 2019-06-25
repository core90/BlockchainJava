# FirstChain
## Blockchain created on Java with Web-UI

### Build with:
- Netbeans 8.2
- MySql Workbench 8.0
- Glassfish 4.1.2 Server
- JSF 2.2
- Bootstrap 
- Hibernate 4.3 
- JPA 2.1

### Features:

- Landing Page with Infos

- Creating new Wallets
  - To register new Wallet you need to enter username and password
  - Private and Public Key will be created with `KeyPairGenerator` and `ECDSA` using a `SecureRandomNumber SHA1 Hash`
  
- Login/Logout from Wallets
  - Login with username and password
  - Public Key and balance will be shown
  - balance is calculated via inputs to this wallet
  
- Creating new transactions (Send and receive FirstCoins)
  - Transactions will be checked if:
    - Public Key exists
    - Signature can be verified
    - Sender has enough funds
    - UTXO's will be updated
    
- Mining new blocks
  - new block will be automatically mined if new transaction is verified
  - Mining will take time until target hash is reached
  - New Block will be added to the chain
  - difficulty can be adjusted manually in code
  
- Blockexplorer
  - Contains infos about the blockchain and transactions in every block
  
- Security Provider = `BouncyCastle.org`

- Data is saved with `JPA/Hibernate` on a `MySQL Server` (Work in Progress)
