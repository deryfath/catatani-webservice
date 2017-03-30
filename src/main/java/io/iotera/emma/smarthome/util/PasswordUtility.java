package io.iotera.emma.smarthome.util;

import io.iotera.util.Encrypt;

public class PasswordUtility {

    public static String hashPassword(byte[] password, byte[] paruru) {
        return Encrypt.SHA256(Encrypt.concatBytes(paruru, password));
    }

}
