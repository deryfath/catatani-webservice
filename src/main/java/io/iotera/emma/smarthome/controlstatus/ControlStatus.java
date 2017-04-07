package io.iotera.emma.smarthome.controlstatus;

import io.iotera.emma.smarthome.preference.DevicePref;

public abstract class ControlStatus {

    protected final String control;
    protected final String oldState;
    protected final String currentState;
    protected String info;
    protected boolean updateInfo = false;

    protected ControlStatus(String control, String oldState) {
        this.control = control;
        this.oldState = oldState;
        if (control != null) {
            this.currentState = currentStatusAfterControl(control, oldState);
        } else {
            this.currentState = currentStateOld(oldState);
        }
    }

    protected ControlStatus(String oldState) {
        this(null, oldState);
    }

    public static ControlStatus buildByCategory(String control, String oldState, int dc) {
        if (dc == DevicePref.CAT_AC) {
            return buildAC(control, oldState);
        } else if (dc == DevicePref.CAT_TV) {
            return buildOff(control, oldState);
        }
        return buildOnOff(control, oldState);
    }

    public static ControlStatus buildByCategory(String oldState, int dc) {
        if (dc == DevicePref.CAT_AC) {
            return buildAC(oldState);
        } else if (dc == DevicePref.CAT_TV) {
            return buildOff(oldState);
        }
        return buildOnOff(oldState);
    }

    public static ControlStatus buildOnOff(String control, String oldState) {
        return new OnOffControlStatus(control, oldState);
    }

    public static ControlStatus buildOnOff(String oldState) {
        return new OnOffControlStatus(oldState);
    }

    public static ControlStatus buildOff(String control, String oldState) {
        return new OffControlStatus(control, oldState);
    }

    public static ControlStatus buildOff(String oldState) {
        return new OffControlStatus(oldState);
    }

    public static ControlStatus buildAC(String control, String oldState) {
        return new ACControlStatus(control, oldState);
    }

    public static ControlStatus buildAC(String oldState) {
        return new ACControlStatus(oldState);
    }

    protected abstract String currentStatusAfterControl(String control, String oldState);

    protected abstract String currentStateOld(String oldState);

    public abstract boolean isOn();

    public final String getControl() {
        return control;
    }

    public final String getOldState() {
        return oldState;
    }

    public final String getCurrentState() {
        return currentState;
    }

    public final boolean isUpdateInfo() {
        return updateInfo;
    }

    public final void setInfo(String info, boolean updateInfo) {
        this.info = info;
        this.updateInfo = updateInfo;
    }

    public final String getInfo() {
        return info;
    }

    @Override
    public final String toString() {
        return currentState;
    }

}
