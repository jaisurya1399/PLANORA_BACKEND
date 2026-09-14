package com.projectmanagement.app.auth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class TotpService {
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    public boolean verify(String secret, String code) {
        if (secret == null || code == null || !code.matches("\\d{6}"))
            return false;
        long counter = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        for (long offset = -1; offset <= 1; offset++) {
            if (generateCode(secret, counter + offset).equals(code))
                return true;
        }
        return false;
    }

    public String generateCode(String secret, long counter) {
        try {
            byte[] key = decodeBase32(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to generate MFA code", ex);
        }
    }

    public String buildOtpAuthUrl(String secret, String email, String issuer) {
        return "otpauth://totp/" + urlEncode(issuer) + ":" + urlEncode(email)
                + "?secret=" + secret + "&issuer=" + urlEncode(issuer) + "&algorithm=SHA1&digits=6&period=30";
    }

    public String hashRecoveryCode(String code) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String encodeBase32(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte value : bytes) {
            buffer = (buffer << 8) | (value & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                out.append(BASE32.charAt((buffer >> bitsLeft) & 31));
            }
        }
        if (bitsLeft > 0)
            out.append(BASE32.charAt((buffer << (5 - bitsLeft)) & 31));
        return out.toString();
    }

    private byte[] decodeBase32(String input) {
        String value = input.replace("=", "").replaceAll("\\s+", "").toUpperCase();
        ByteBuffer out = ByteBuffer.allocate(value.length() * 5 / 8 + 1);
        int buffer = 0;
        int bitsLeft = 0;
        for (char c : value.toCharArray()) {
            int index = BASE32.indexOf(c);
            if (index < 0)
                throw new IllegalArgumentException("Invalid MFA secret");
            buffer = (buffer << 5) | index;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                out.put((byte) ((buffer >> bitsLeft) & 0xff));
            }
        }
        byte[] result = new byte[out.position()];
        out.flip();
        out.get(result);
        return result;
    }

    private String urlEncode(String value) {
        return value.replace("%", "%25").replace(" ", "%20").replace("@", "%40");
    }
}
