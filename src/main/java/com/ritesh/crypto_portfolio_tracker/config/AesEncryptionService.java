package com.ritesh.crypto_portfolio_tracker.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Service
public class AesEncryptionService {

    @Value("${aes.secret}")
    private String aesKey;

    private SecretKey key() {
        return new SecretKeySpec(aesKey.getBytes(), "AES");
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key());
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public String decrypt(String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key());
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }
}
