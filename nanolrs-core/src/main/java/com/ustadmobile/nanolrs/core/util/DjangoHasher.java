package com.ustadmobile.nanolrs.core.util;

/**
 * Created by varuna on 10/18/2017.
 */

/* Example implementation of password hasher similar on Django's PasswordHasher
 * Requires Java8 (but should be easy to port to older JREs)
 * Currently it would work only for pbkdf2_sha256 algorithm
 *
 * Django code: https://github.com/django/django/blob/1.6.5/django/contrib/auth/hashers.py#L221
 *
 *
 * UPDATE: NOT USING SHA255. INSTEAD USING SHA1
 */

/*
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
*/

import java.io.UnsupportedEncodingException;


import java.nio.charset.Charset;

import java.security.SecureRandom;
import java.util.Random;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class DjangoHasher {

    public final Integer DEFAULT_ITERATIONS = 12000;
    public final String algorithm = "pbkdf2_sha1";
    public final String algorithm256 = "pbkdf2_sha256";

    public DjangoHasher() {}


    public String getEncodedHashJava8SHA256(String password, String salt, int iterations) {
        // Returns only the last part of whole encoded password
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could NOT retrieve PBKDF2WithHmacSHA256 algorithm");
            System.exit(1);
        }
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(Charset.forName("UTF-8")), iterations, 256);
        SecretKey secret = null;
        try {
            secret = keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            System.out.println("Could NOT generate secret key");
            e.printStackTrace();
        }

        byte[] rawHash = secret.getEncoded();
        char[] a = Base64CoderNanoLrs.encode(rawHash);
        return new String(a);

    }

    public String getEncodedHashJava7SHA1(String password, String salt, int iterations) {
        // Returns only the last part of whole encoded password
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could NOT retrieve PBKDF2WithHmacSHA256 algorithm");
            System.exit(1);
        }
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(Charset.forName("UTF-8")), iterations, 256);
        SecretKey secret = null;
        try {
            secret = keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            System.out.println("Could NOT generate secret key");
            e.printStackTrace();
        }

        byte[] rawHash = secret.getEncoded();
        char[] a = Base64CoderNanoLrs.encode(rawHash);
        return new String(a);

    }



    /*
    // Returns only the last part of whole encoded password using Bouncy castle for Java 7
    public String getEncodedHash7SHA256(String password, String salt, int iterations) {
        // Returns only the last part of whole encoded password
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());

        try {
            gen.init(password.getBytes("UTF-8"), salt.getBytes(), iterations);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(DjangoHasher.class.getName() + "EXCEPTION" + ex);
        }
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();

        //byte[] hashBase64 = Base64.encodeBase64(dk);
        byte[] hashBase64 = new String(Base64CoderNanoLrs.encode(dk)).getBytes();
        return new String(hashBase64);
    }
    */


    // returns hashed password, along with algorithm, number of iterations and salt
    public String encode(String password, String salt, int iterations) {
        String hash = getEncodedHashJava7SHA1(password, salt, iterations);
        return String.format("%s$%d$%s$%s", algorithm, iterations, salt, hash);
    }

    public String encode(String password, String salt) {
        return this.encode(password, salt, this.DEFAULT_ITERATIONS);
    }

    public String encode(String password){
        return this.encode(password, createRandomSaltString(), this.DEFAULT_ITERATIONS);
    }

    public boolean checkPassword(String password, String hashedPassword) {
        // hashedPassword consist of: ALGORITHM, ITERATIONS_NUMBER, SALT and
        // HASH; parts are joined with dollar character ("$")
        String[] parts = hashedPassword.split("\\$");
        if (parts.length != 4) {
            // wrong hash format
            return false;
        }
        Integer iterations = Integer.parseInt(parts[1]);
        String salt = parts[2];
        String hash = encode(password, salt, iterations);

        return hash.equals(hashedPassword);
    }

    /*
    // returns hashed password, along with algorithm, number of iterations and salt
    public String encodeSHA256(String password, String salt, int iterations) {
        String hash = getEncodedHash7SHA256(password, salt, iterations);
        return String.format("%s$%d$%s$%s", algorithm256, iterations, salt, hash);
    }

    public String encodeSHA256(String password, String salt) {
        return this.encodeSHA256(password, salt, this.DEFAULT_ITERATIONS);
    }

    public String encodeSHA256(String password){
        return this.encodeSHA256(password, createRandomSaltString(), this.DEFAULT_ITERATIONS);
    }

    public boolean checkPasswordSHA256(String password, String hashedPassword) {
        // hashedPassword consist of: ALGORITHM, ITERATIONS_NUMBER, SALT and
        // HASH; parts are joined with dollar character ("$")
        String[] parts = hashedPassword.split("\\$");
        if (parts.length != 4) {
            // wrong hash format
            return false;
        }
        Integer iterations = Integer.parseInt(parts[1]);
        String salt = parts[2];
        String hash = encodeSHA256(password, salt, iterations);

        return hash.equals(hashedPassword);
    }
    */

    /**
     * Creates random salt of 12 characters length.
     * @return
     */
    public String createRandomSaltString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 13) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }


    //Ignore below

    // Following examples can be generated at any Django project:
    //
    //  >>> from django.contrib.auth.hashers import make_password
    //  >>> make_password('mystery', hasher='pbkdf2_sha256')  # salt would be randomly generated
    //  'pbkdf2_sha256$10000$HqxvKtloKLwx$HdmdWrgv5NEuaM4S6uMvj8/s+5Yj+I/d1ay6zQyHxdg='
    //  >>> make_password('mystery', salt='mysalt', hasher='pbkdf2_sha256')
    //  'pbkdf2_sha256$10000$mysalt$KjUU5KrwyUbKTGYkHqBo1IwUbFBzKXrGQgwA1p2AuY0='
    //
    //
    // mystery
    // pbkdf2_sha256$10000$qx1ec0f4lu4l$3G81rAm/4ng0tCCPTrx2aWohq7ztDBfFYczGNoUtiKQ=
    //
    // s3cr3t
    // pbkdf2_sha256$10000$BjDHOELBk7fR$xkh1Xf6ooTqwkflS3rAiz5Z4qOV1Jd5Lwd8P+xGtW+I=
    //
    // puzzle
    // pbkdf2_sha256$10000$IFYFG7hiiKYP$rf8vHYFD7K4q2N3DQYfgvkiqpFPGCTYn6ZoenLE3jLc=
    //
    // riddle
    // pbkdf2_sha256$10000$A0S5o3pNIEq4$Rk2sxXr8bonIDOGj6SU4H/xpjKHhHAKpFXfmNZ0dnEY=


    private static void runTests() {
        System.out.println("===========================");
        System.out.println("= Testing password hasher =");
        System.out.println("===========================");
        System.out.println();

        System.out.println();
        passwordShouldMatch("mystery", "pbkdf2_sha256$10000$qx1ec0f4lu4l$3G81rAm/4ng0tCCPTrx2aWohq7ztDBfFYczGNoUtiKQ=");
        passwordShouldMatch("mystery", "pbkdf2_sha256$10000$mysalt$KjUU5KrwyUbKTGYkHqBo1IwUbFBzKXrGQgwA1p2AuY0=");  // custom salt
        passwordShouldMatch("s3cr3t", "pbkdf2_sha256$10000$BjDHOELBk7fR$xkh1Xf6ooTqwkflS3rAiz5Z4qOV1Jd5Lwd8P+xGtW+I=");
        passwordShouldMatch("puzzle", "pbkdf2_sha256$10000$IFYFG7hiiKYP$rf8vHYFD7K4q2N3DQYfgvkiqpFPGCTYn6ZoenLE3jLc=");
        passwordShouldMatch("riddle", "pbkdf2_sha256$10000$A0S5o3pNIEq4$Rk2sxXr8bonIDOGj6SU4H/xpjKHhHAKpFXfmNZ0dnEY=");

        System.out.println();
        passwordShouldNotMatch("foo", "");
        passwordShouldNotMatch("mystery", "pbkdf2_md5$10000$qx1ec0f4lu4l$3G81rAm/4ng0tCCPTrx2aWohq7ztDBfFYczGNoUtiKQ=");
        passwordShouldNotMatch("mystery", "pbkdf2_sha1$10000$qx1ec0f4lu4l$3G81rAm/4ng0tCCPTrx2aWohq7ztDBfFYczGNoUtiKQ=");
        passwordShouldNotMatch("mystery", "pbkdf2_sha256$10001$Qx1ec0f4lu4l$3G81rAm/4ng0tCCPTrx2aWohq7ztDBfFYczGNoUtiKQ=");
        passwordShouldNotMatch("mystery", "pbkdf2_sha256$10001$qx1ec0f4lu4l$3G81rAm/4ng0tCCPTrx2aWohq7ztDBfFYczGNoUtiKQ=");
        passwordShouldNotMatch("mystery", "pbkdf2_sha256$10000$qx7ztDBfFYczGNoUtiKQ=");
        passwordShouldNotMatch("s3cr3t", "pbkdf2_sha256$10000$BjDHOELBk7fR$foobar");
        passwordShouldNotMatch("puzzle", "pbkdf2_sha256$10000$IFYFG7hiiKYP$rf8vHYFD7K4q2N3DQYfgvkiqpFPGCTYn6ZoenLE3jLcX");
    }

    private static void passwordShouldMatch(String password, String expectedHash) {
        DjangoHasher hasher = new DjangoHasher();

        if (hasher.checkPassword(password, expectedHash)) {
            System.out.println(" => OK");
        } else {
            String[] parts = expectedHash.split("\\$");
            if (parts.length != 4) {
                System.out.printf(" => Wrong hash provided: '%s'\n", expectedHash);
                return;
            }
            String salt = parts[2];
            String resultHash = hasher.encode(password, salt);
            String msg = " => Wrong! Password '%s' hash expected to be '%s' but is '%s'\n";
            System.out.printf(msg, password, expectedHash, resultHash);
        }
    }

    private static void passwordShouldNotMatch(String password, String expectedHash) {
        DjangoHasher hasher = new DjangoHasher();

        if (hasher.checkPassword(password, expectedHash)) {
            System.out.printf(" => Wrong (password '%s' did '%s' match but were not supposed to)\n", password, expectedHash);
        } else {
            System.out.println(" => OK (password didn't match)");
        }
    }

    //Testing this:

    public byte[] deriveKey(String p, byte[] s, int i, int l) throws Exception {
        PBEKeySpec ks = new PBEKeySpec(p.toCharArray(), s, i, l);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(ks).getEncoded();
    }

    public String encryptCustom7(String s, String p, int iteration) throws Exception {
        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");

        // Generate 160 bit Salt for Encryption Key
        byte[] esalt = new byte[20]; r.nextBytes(esalt);
        // Generate 128 bit Encryption Key
        byte[] dek = deriveKey(p, esalt, 100000, 128);

        // Perform Encryption
        SecretKeySpec eks = new SecretKeySpec(dek, "AES");
        Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
        //byte[] es = c.doFinal(s.getBytes(StandardCharsets.UTF_8));
        byte[] es = c.doFinal(s.getBytes(Charset.forName("UTF-8")));

        // Generate 160 bit Salt for HMAC Key
        byte[] hsalt = new byte[20]; r.nextBytes(hsalt);
        // Generate 160 bit HMAC Key
        byte[] dhk = deriveKey(p, hsalt,iteration, 160);

        // Perform HMAC using SHA-256
        SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(hks);
        byte[] hmac = m.doFinal(es);

        // Construct Output as "ESALT + HSALT + CIPHERTEXT + HMAC"
        byte[] os = new byte[40 + es.length + 32];
        System.arraycopy(esalt, 0, os, 0, 20);
        System.arraycopy(hsalt, 0, os, 20, 20);
        System.arraycopy(es, 0, os, 40, es.length);
        System.arraycopy(hmac, 0, os, 40 + es.length, 32);

        // Return a Base64 Encoded String
        String hash = new String(Base64CoderNanoLrs.encode(os));

        return hash;
    }

}
