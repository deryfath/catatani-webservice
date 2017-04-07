package io.iotera.emma.smarthome.controlstatus;


public class OffControlStatus extends ControlStatus {

    protected OffControlStatus(String oldState) {
        super(oldState);
    }

    protected OffControlStatus(String control, String oldState) {
        super(control, oldState);
    }

    @Override
    protected String currentStatusAfterControl(String control, String oldState) {
        return "{\"st\":0}";
    }

    @Override
    protected String currentStateOld(String oldState) {
        return "{\"st\":0}";
    }

    @Override
    public boolean isOn() {
        return false;
    }
}
