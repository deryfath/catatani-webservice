package io.iotera.emma.smarthome.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ResourceUtility {

    public static String resourceImagePath(String hostPath, String path) {
        return hostPath + "/res/image/" + path;
    }

    public static boolean save(byte[] data, String attachment, String path, String filename) {
        // File
        File file = new File(store(attachment, path), filename);

        try {
            // Insert file
            OutputStream os = new FileOutputStream(file);
            os.write(data);
            os.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean delete(String attachment, String path, String filename) {
        // File
        File file = new File(store(attachment, path), filename);
        if (file.exists()) {
            return file.delete();
        }

        return true;
    }

    private static File store(String attachment, String path) {
        // Path
        File store;
        String fullPath = attachment + '/' + path;
        if (new File(fullPath).isAbsolute()) {
            store = new File(fullPath);
        } else {
            store = new File(fullPath);
        }
        if (!store.exists()) {
            store.mkdirs();
        }

        return store;
    }

    public static String filename(String savePath) {
        if (savePath == null) {
            return null;
        }

        String[] token = savePath.split("/");
        return token[token.length - 1];
    }

    public static String hubPath(long accountId, String type, String itemId) {
        String path = "hub/" + accountId + '/' + type;
        if (itemId != null) {
            path += '/' + itemId;
        }
        return path;
    }

    public static String hubPath(long accountId, String type) {
        return hubPath(accountId, type, null);
    }

    public static String clientPath(long clientId, String type, String itemId) {
        String path = "client/" + clientId + '/' + type;
        if (itemId != null) {
            path += '/' + itemId;
        }
        return path;
    }

    public static String clientPath(long accountId, String type) {
        return clientPath(accountId, type, null);
    }

}
