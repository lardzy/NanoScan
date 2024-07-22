package com.gttcgf.nanoscan.tools;

import android.content.Context;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class RSAEncrypt {

    private RSAEncrypt() {
    }

    public static String encryptData(String data, String publicKeyStr) throws Exception {
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());

        // 生成公钥对象
        byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 生成 AES 会话密钥
        KeyGenerator kenGen = KeyGenerator.getInstance("AES");
        kenGen.init(128);
        SecretKey sessionKey = kenGen.generateKey();

        // 使用 公钥 使用 RSA 加密 会话密钥
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "BC");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encSessionKey = rsaCipher.doFinal(sessionKey.getEncoded());
        // 以上验证没问题
        // 使用 AES 会话密钥加密数据
//        Cipher aesCipher = Cipher.getInstance("AES/EAX/NoPadding", "BC");
//        Cipher aesCipher = Cipher.getInstance("AES/EAX/PKCS7Padding", "BC");
//        Cipher aesCipher = Cipher.getInstance("AES/EAX/NoPadding", "BC");

        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);

        EAXBlockCipher eaxCipher = new EAXBlockCipher(new AESEngine());
        AEADParameters aeadParameters = new AEADParameters(new KeyParameter(sessionKey.getEncoded()), 128, nonce);
        eaxCipher.init(true, aeadParameters);

        byte[] ciphertext = new byte[eaxCipher.getOutputSize(data.getBytes(StandardCharsets.UTF_8).length)];
        int len = eaxCipher.processBytes(data.getBytes(StandardCharsets.UTF_8), 0, data.getBytes(StandardCharsets.UTF_8).length, ciphertext, 0);
        len += eaxCipher.doFinal(ciphertext, len);

        // 生成 tag
        byte[] tag = eaxCipher.getMac();

        byte[] encryptedData = new byte[ciphertext.length - 16];
        System.arraycopy(ciphertext, encryptedData.length - 16, encryptedData, 0, encryptedData.length);

        // 拼接加密数据
        byte[] combined = new byte[encSessionKey.length + nonce.length + tag.length + encryptedData.length];
        System.arraycopy(encSessionKey, 0, combined, 0, encSessionKey.length);
        System.arraycopy(nonce, 0, combined, encSessionKey.length, nonce.length);
        System.arraycopy(tag, 0, combined, encSessionKey.length + nonce.length, tag.length);
        System.arraycopy(encryptedData, 0, combined, encSessionKey.length + nonce.length + tag.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }
//        return "FFm1b61gTgp9i1CdtsB6S2Yriy/EjBCzk+XJeV7d7Mv1MNcYLdVF1f4zdCarpaKbp/Zli8fhCDWh6wHjAbGp/MLQCnbl2q/qlqnLfYnoKGUZHgg51Lz+pRGvXONunNCJRcgJdqvV15154K8ism0+uVzF/1vqGiVHAQ7K3CWCGMek0oXF/prGNTR+rgSRB+bIuqxpqU/rMI0ewrn5/97dqoDYsmvemA3UQMxweiQ5NcuVvSH2QyfZku5OhrUGIgwJsFVD1vu2ScQISjc7AjsDJ+ZVNo5fv7XW24cNO2xSyzX6MNsZ81av+ngFDTuLDmfKKEwn9TSc7QcMoXAgvr//9j69gxqy+IQrpvDtSAdX9YhDLl2hRXuMZQa2RuoQfov4hdK3fx/sYCbtilavb1Jk9b8=";

    public static String loadPublicKey(Context context, int rawPublicKeyResId) throws Exception {
        InputStream inputStream = context.getResources().openRawResource(rawPublicKeyResId);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        br.close();

        String publicKeyPEM = sb.toString();
        publicKeyPEM = publicKeyPEM.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");

        return publicKeyPEM;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
