package io.iotera.emma.smarthome.controlstatus;

public class ACControlStatus extends ControlStatus {

    /////////////
    // Builder //
    /////////////

    public static ACControlStatus build(String control) {
        return new ACControlStatus(control);
    }

    public static ACControlStatus build(ControlStatus control) {
        return build(control.control);
    }

    ///////////////////////
    // AC Control Status //
    ///////////////////////

    private ACControlStatus(String control) {
        super(control);
        if (control.equals("0")) {
            this.info = null;
            updateInfo = true;
        }
    }

    @Override
    public boolean update() {
        return super.update() || control.equals("") || control.equals("");
    }

    @Override
    public int getState() {
        if (control.equals("1")) {
            return 24;
        }
        return super.getState();
    }

}
