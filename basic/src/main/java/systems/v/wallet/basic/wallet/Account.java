package systems.v.wallet.basic.wallet;

import com.alibaba.fastjson.annotation.JSONField;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;

import systems.v.vsys.Vsys;
import systems.v.wallet.basic.utils.Base58;

public class Account implements AccountBalance {
    private long nonce;
    private String network;
    private String seed;
    private String address;
    private String publicKey;

    @JSONField(serialize = false)
    private systems.v.vsys.Account account;

    // balance info
    private long regular; // total balance
    private long mintingAverage;
    private long available;
    private long effective;
    private int height;

    public Account() {
    }

    public Map<String, String> createAddress(String seed,long nonce ,String network,String version){
        Map<String, String> map = new HashMap<String, String>();
        String strSeed = String.valueOf(nonce) + seed;
        byte[] btSeed = Vsys.hashChain(strSeed.getBytes());

        MySecureRandomProvider pri = new MySecureRandomProvider();
        try {
            pri.SetBytes(MessageDigest.getInstance("SHA-256").digest(btSeed));
        } catch (NoSuchAlgorithmException e) {
        }

        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST,pri);
        Curve25519KeyPair pair = cipher.generateKeyPair();

        byte[] btPublicKey = Vsys.hashChain(pair.getPublicKey());
        byte[] btWithoutCheck = new byte[22];
        btWithoutCheck[0] = (byte)(Integer.parseInt(version));
        btWithoutCheck[1] = network.getBytes()[0];
        System.arraycopy(btPublicKey, 0, btWithoutCheck, 2, 20);

        byte[] btCheck = Vsys.hashChain(btWithoutCheck);
        byte[] btAddress = new byte[26];
        System.arraycopy(btWithoutCheck, 0, btAddress, 0, 22);
        System.arraycopy(btCheck, 0, btAddress, 22, 4);

        map.put("address", Base58.encode(btAddress));
        map.put("publicKey", Base58.encode(pair.getPublicKey()));

        return map;
    }

    public Account(String seed, long nonce, String network,String version, systems.v.vsys.Account account) {
        this.seed = seed;
        this.nonce = nonce;
        this.network = network;
        this.account = account;
        Map<String, String> map = createAddress(seed,nonce,network,version);
        this.address = map.get("address");
        this.publicKey = map.get("publicKey");
    }

    public void updateBalance(AccountBalance balance) {
        this.regular = balance.getRegular();
        this.mintingAverage = balance.getMintingAverage();
        this.available = balance.getAvailable();
        this.effective = balance.getEffective();
        this.height = balance.getHeight();
    }

    public String getAccountSeed() {
        if (account == null) {
            return "";
        }
        return account.accountSeed();
    }

    public String getPrivateKey() {
        if (account == null) {
            return "";
        }
        return account.privateKey();
    }

    public String getSignature(byte[] data) {
        if (account == null) {
            return "";
        }
        return account.signData(data);
    }

    public boolean isColdAccount() {
        return account == null;
    }

    @Override
    public String toString() {
        return "Nonce: " + nonce + "\nAccount seed: " + getAccountSeed() + "\nPrivate Key: "
                + getPrivateKey() + "\nPublic Key: " + getPublicKey() + "\nAddress: " + getAddress();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public systems.v.vsys.Account getAccount() {
        return account;
    }

    public void setAccount(systems.v.vsys.Account account) {
        this.account = account;
    }

    public long getRegular() {
        return regular;
    }

    public void setRegular(long regular) {
        this.regular = regular;
    }

    public long getMintingAverage() {
        return mintingAverage;
    }

    public void setMintingAverage(long mintingAverage) {
        this.mintingAverage = mintingAverage;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public long getEffective() {
        return effective;
    }

    public void setEffective(long effective) {
        this.effective = effective;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
