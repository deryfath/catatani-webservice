package io.iotera.emma.smarthome.controlstatus;

import io.iotera.util.Number;

public class ControlStatus {

    /////////////
    // Builder //
    /////////////

    public static ControlStatus build(String control, String info) {
        return new ControlStatus(control, info);
    }

    public static ControlStatus build(String control) {
        return new ControlStatus(control);
    }

    public static ControlStatus build(int control, String info) {
        return new ControlStatus(String.valueOf(control), info);
    }

    public static ControlStatus build(int control) {
        return new ControlStatus(String.valueOf(control));
    }

    public static ControlStatus build(boolean on, String info) {
        return new ControlStatus(on ? "1" : "0", info);
    }

    public static ControlStatus build(boolean on) {
        return new ControlStatus(on ? "1" : "0");
    }

    ////////////////////
    // Control Status //
    ////////////////////

    protected final String control;

    protected String info;
    protected boolean updateInfo = false;

    protected ControlStatus(String control, String info) {
        this.control = control;
        this.info = info;
        this.updateInfo = true;
    }

    protected ControlStatus(String control) {
        this.control = control;
    }

    public final String get() {
        return control;
    }

    public boolean update() {
        return Number.isInt(control);
    }

    public boolean isOn() {
        return !control.equals("0");
    }

    public int getState() {
        if (Number.isInt(control)) {
            return Integer.parseInt(control);
        }
        return 0;
    }

    public final boolean updateInfo() {
        return updateInfo;
    }

    public final String getInfo() {
        return info;
    }

    @Override
    public final String toString() {
        return get();
    }

}
