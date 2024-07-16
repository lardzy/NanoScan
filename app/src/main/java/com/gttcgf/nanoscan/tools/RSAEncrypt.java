package com.gttcgf.nanoscan.tools;

import android.content.Context;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAEncrypt {
    public static byte[] encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plainText.getBytes());
    }
    public static PublicKey loadPublicKey(Context context, int resourceId) throws Exception {
        InputStream is = context.getResources().openRawResource(resourceId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        String publicKeyPEM = sb.toString();
        // 去掉头尾标识
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        // 解码Base64
        byte[] encoded = Base64.decode(publicKeyPEM, Base64.DEFAULT);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }
}
