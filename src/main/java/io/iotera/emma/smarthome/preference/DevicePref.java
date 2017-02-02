package io.iotera.emma.smarthome.preference;

public class DevicePref {

    public static final int CAT_PLUG = 1;
    public static final int CAT_REMOTE = 2;
    public static final int CAT_CAMERA = 3;

    public static final int CAT_APPLIANCE = 900;
    public static final int CAT_AC = 901;
    public static final int CAT_TV = 902;
    public static final int CAT_RF_SWITCH = 909;

    public static final int ONE_THOUSAND = 1000;

    public static final int TYPE_RF_SWITCH_MIN = 0;
    public static final int TYPE_RF_SWITCH_MAX = 999999;
    public static final int ONE_MILLION = 1000000;

    public static boolean isAppliance(int category) {
        return (category / 900) == 1;
    }

}
