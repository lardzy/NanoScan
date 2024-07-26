package com.gttcgf.nanoscan.tools;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;

public class RSAEncrypt {

    private RSAEncrypt() {
    }

    public static String encryptData(String data, String publicKeyStr) throws Exception {
        // 生成公钥对象
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 使用 公钥 使用 RSA 加密 会话密钥
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encSessionKey = rsaCipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encSessionKey);
    }

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
