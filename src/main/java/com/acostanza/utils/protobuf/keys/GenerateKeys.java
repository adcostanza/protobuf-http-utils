package com.acostanza.utils.protobuf.keys;

import java.security.PrivateKey;
import java.security.PublicKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;


/**
 * these are to help remove the need to copy and paste these commonly used methods
 * for public and private key generation in use with JWTs.
 */
//Copied from https://www.mkyong.com/java/java-asymmetric-cryptography-example/
public class GenerateKeys {
    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public GenerateKeys(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keylength);
    }

    public void createKeys() {
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void writeToFile(String path, byte[] key) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public static KeyPair generateKeys() {
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(2048);
            gk.createKeys();
            gk.writeToFile("keys/public", gk.getPublicKey().getEncoded());
            gk.writeToFile("keys/private", gk.getPrivateKey().getEncoded());

            return new KeyPair(gk.getPublicKey(), gk.getPrivateKey());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}