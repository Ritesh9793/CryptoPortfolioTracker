package com.ritesh.crypto_portfolio_tracker.config;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {

    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 32; // 32 bytes = AES-256

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoUtil(String key) {

        if (key == null) {
            throw new IllegalArgumentException("crypto.secret.key is required");
        }

        byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);

        if (keyBytes.length != KEY_SIZE) {
            throw new IllegalArgumentException(
                    "crypto.secret.key must be EXACTLY 32 ASCII characters"
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] randomIV() {
        byte[] iv = new byte[16]; // AES block size
        secureRandom.nextBytes(iv);
        return iv;
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = randomIV();

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new IvParameterSpec(iv)
            );

            byte[] cipherBytes = cipher.doFinal(
                    plain.getBytes(StandardCharsets.UTF_8)
            );

            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);

            byte[] iv = new byte[16];
            byte[] cipherBytes = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new IvParameterSpec(iv)
            );

            return new String(
                    cipher.doFinal(cipherBytes),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
