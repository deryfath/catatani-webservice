package io.iotera.emma.smarthome.utility;

import io.iotera.util.Encrypt;

import java.security.SecureRandom;

public class ESUtility {

    public static String randomString(int length) {
        String cs = "0123456789abcdefghijklmnopqrstuvwxyz";
        SecureRandom rand = new SecureRandom();

        StringBuilder randomBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            randomBuilder.append(cs.charAt(rand.nextInt(cs.length())));
        }
        return randomBuilder.toString();
    }

    public static String randomStringCase(int length) {
        String cs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rand = new SecureRandom();

        StringBuilder randomBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            randomBuilder.append(cs.charAt(rand.nextInt(cs.length())));
        }
        return randomBuilder.toString();
    }

    public static String randomLong() {
        SecureRandom rand = new SecureRandom();
        long randomLong = Math.abs(rand.nextLong());

        return String.valueOf(randomLong);
    }

    public static String hashPassword(byte[] password, byte[] paruru) {
        return Encrypt.SHA256(Encrypt.concatBytes(paruru, password));
    }

}
