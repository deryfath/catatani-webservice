package io.iotera.emma.smarthome.preference;

public class ESDeviceCategory {
    public static final int PLUG = 1;
    public static final int REMOTE = 2;
    public static final int CAMERA = 3;

    public static final int APPLIANCE = 900;

    public static boolean isAppliance(int category) {
        return (category / 900) == 1;
    }
}