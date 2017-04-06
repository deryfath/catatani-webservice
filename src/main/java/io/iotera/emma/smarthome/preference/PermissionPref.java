package io.iotera.emma.smarthome.preference;

public class PermissionPref {

    public static final String OWNER = "owner";
    public static final String ADMIN = "admin";
    public static final String MEMBER = "member";

    public static boolean isOwner(String permission) {
        return permission.equalsIgnoreCase("owner");
    }

    public static boolean isOwnerOrAdmin(String permission) {
        return permission.equalsIgnoreCase("owner") || permission.equalsIgnoreCase("admin");
    }

}
