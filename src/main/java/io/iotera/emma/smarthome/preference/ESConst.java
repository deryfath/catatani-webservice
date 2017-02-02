package io.iotera.emma.smarthome.preference;

public class ESConst {

    public static class DevicePreferences {
        public static final int PLUG = 1;
        public static final int REMOTE = 2;
        public static final int IP_CAMERA = 3;

        public static final int ONE_THOUSAND = 1000;

    }

    public static class ESApplianceTypecode {
        public static final int AC_DEFAULT = 1000000;
        public static final int TV_DEFAULT = 2000000;
        public static final int RF_SWITCH_DEFAULT = 9000000;

        public static final int ONE_MILLION = 1000000;

        public static boolean isAirConditioner(int code) {
            return (code / ONE_MILLION) == 1;
        }

        public static boolean isTelevision(int code) {
            return (code / ONE_MILLION) == 2;
        }

        public static boolean isRFSwitchDefault(int code) {
            return code == RF_SWITCH_DEFAULT;
        }

        public static boolean isRFSwitch(int code) {
            if (code == RF_SWITCH_DEFAULT) {
                return true;
            }

            return (code / ONE_MILLION) == 9;
        }

        public static String toString(int code) {
            if (isAirConditioner(code)) {
                return "AC";
            } else if (isTelevision(code)) {
                return "TV";
            } else if (isRFSwitch(code)) {
                return "RF_SWITCH";
            }
            return "null";
        }
    }
}
