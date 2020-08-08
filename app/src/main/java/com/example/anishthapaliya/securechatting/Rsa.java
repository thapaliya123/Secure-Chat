package com.example.anishthapaliya.securechatting;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;


import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class Rsa {

    private String messageToEncrypt;
    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(encrypted);
    }


    public static void readWriteKEY() throws Exception {
        //generate a key pare and store
        KeyPair keyPair = buildKeyPair();

        PublicKey pubKey = keyPair.getPublic();

        PrivateKey privateKey = keyPair.getPrivate();

        String outFile = "C:\\Users\\Anish thapaliya\\Desktop\\";
        FileOutputStream out = new FileOutputStream(outFile + "ss.key");
        out.write(privateKey.getEncoded());
        out.close();

        out = new FileOutputStream(outFile + "sss.pub");
        out.write(pubKey.getEncoded());
        out.close();

        System.out.println("Success");;

        //end of generation






    }

    public static PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Read all bytes from the private key file
        Path path = Paths.get("C:\\Users\\Anish thapaliya\\Desktop\\ss.key");
        byte[] bytes = 	Files.readAllBytes(path);



        // Generate private key.
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pvt = kf.generatePrivate(ks);
        return pvt;
    }

    public static PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {



        // Read all the public key bytes
        Path path1 = Paths.get("C:\\Users\\Anish thapaliya\\Desktop\\sss.pub");
        byte[] bytes1 = Files.readAllBytes(path1);

        // Generate public key.
        X509EncodedKeySpec ks1 = new X509EncodedKeySpec(bytes1);
        KeyFactory kf1 = KeyFactory.getInstance("RSA");
        PublicKey pub = kf1.generatePublic(ks1);
        return pub;

    }

   public static void main(String [] args) throws Exception {
       try {
           Rsa.readWriteKEY();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
    }


