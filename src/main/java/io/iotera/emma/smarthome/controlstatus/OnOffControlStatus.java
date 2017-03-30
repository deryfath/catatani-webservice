package io.iotera.emma.smarthome.controlstatus;

public class OnOffControlStatus extends ControlStatus {

    protected boolean on;

    protected OnOffControlStatus(String input, String oldState) {
        super(input, oldState);
    }

    protected OnOffControlStatus(String oldState) {
        super(oldState);
    }

    @Override
    protected String currentStatusAfterControl(String control, String oldState) {
        if (control.equals("0")) {
            on = false;
            return "{\"st\":0}";
        }
        on = true;
        return "{\"st\":1}";
    }

    @Override
    protected String currentStateOld(String oldState) {
        on = oldState.contains("\"st\":1");
        return oldState;
    }

    @Override
    public boolean isOn() {
        return on;
    }
}
