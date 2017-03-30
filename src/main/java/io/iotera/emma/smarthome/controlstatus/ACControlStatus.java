package io.iotera.emma.smarthome.controlstatus;

import io.iotera.util.Json;
import io.iotera.util.Number;
import org.json.JSONException;
import org.json.JSONObject;

public class ACControlStatus extends ControlStatus {

    protected int state;
    protected String fan;
    protected String swing;
    protected boolean on;

    protected ACControlStatus(String input, String oldState) {
        super(input, oldState);
    }

    protected ACControlStatus(String oldState) {
        super(oldState);
    }

    @Override
    protected String currentStatusAfterControl(String control, String oldState) {

        JSONObject old = Json.parseToJSONObject(oldState);
        if (old == null) {
            old = new JSONObject();
        }
        boolean oldOn = old.optInt("st", 0) != 0;

        if (Number.isInt(control)) {
            if (control.equals("1")) {
                control = "24";
            }

            int st = Integer.parseInt(control);
            try {
                old.put("st", st);
            } catch (JSONException e) {
                //e.printStackTrace();
            }

        } else if (control.equals("fa")) {

            if (oldOn) {
                String curFa = "aut";
                String oldFa = old.optString("fa", "aut");
                if (oldFa.equals("aut")) {
                    curFa = "3";
                } else if (oldFa.equals("3")) {
                    curFa = "2";
                } else if (oldFa.equals("2")) {
                    curFa = "1";
                }

                try {
                    old.put("fa", curFa);
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }

        } else if (control.equals("sw")) {

            if (oldOn) {
                String curSw = "man";
                String oldSw = old.optString("sw", "man");
                if (oldSw.equals("man")) {
                    curSw = "aut";
                }

                try {
                    old.put("sw", curSw);
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }

        } else if (control.equals("co")) {

            if (oldOn) {
                try {
                    old.put("st", 24);
                    old.put("fa", "3");
                    old.put("sw", "aut");
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }
        }

        state = old.optInt("st", 0);
        fan = old.optString("fa", "aut");
        swing = old.optString("sw", "man");
        on = state != 0;

        return old.toString();
    }

    @Override
    protected String currentStateOld(String oldState) {

        JSONObject old = Json.parseToJSONObject(oldState);
        if (old == null) {
            old = new JSONObject();
        }

        state = old.optInt("st", 0);
        fan = old.optString("fa", "aut");
        swing = old.optString("sw", "man");
        on = state != 0;

        return oldState;
    }

    @Override
    public boolean isOn() {
        return on;
    }

    public int getState() {
        return state;
    }

    public String getFan() {
        return fan;
    }

    public String getSwing() {
        return swing;
    }

}
