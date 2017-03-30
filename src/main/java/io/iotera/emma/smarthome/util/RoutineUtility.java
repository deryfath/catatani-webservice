package io.iotera.emma.smarthome.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.util.Json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class RoutineUtility {

    public static String getCronExpression(String daysOfWeek, String time) {

        ArrayNode days = Json.parseToArrayNode(daysOfWeek);
        if (days == null) {
            return null;
        }

        int i = 0;
        int count = 0;
        StringBuilder daysBuilder = new StringBuilder();

        for (JsonNode day : days) {
            int value = day.asInt(0);
            if (value == 1) {
                if (count != 0) {
                    daysBuilder.append(',');
                }
                daysBuilder.append(i);
                ++count;
            }
            ++i;
        }
        if (daysBuilder.length() == 0) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        int hour;
        int minute;

        try {
            Date timeDate = sdf.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(timeDate);

            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);

        } catch (ParseException e) {
            return null;
        }

        return "0 " + minute + " " + hour + " * * " + daysBuilder.toString();
    }

    public static ObjectNode getValidCommands(String commands) {

        ObjectNode commandsObject = Json.parseToObjectNode(commands);
        if (commandsObject == null) {
            return null;
        }

        ObjectNode newCommandsObject = Json.buildObjectNode();
        Iterator<Map.Entry<String, JsonNode>> i$ = commandsObject.fields();

        while (i$.hasNext()) {
            Map.Entry<String, JsonNode> en = i$.next();
            String key = en.getKey();
            JsonNode value = en.getValue();

            if (!value.isObject()) {
                continue;
            }

            if (value.has("dc") && value.has("cos")) {
                JsonNode dc = value.get("dc");
                if (dc.getNodeType() != JsonNodeType.NUMBER) {
                    continue;
                }
                JsonNode cos = value.get("cos");
                if (!cos.isArray()) {
                    continue;
                }

                boolean csv = true;
                for (JsonNode cso : cos) {
                    if (cso.getNodeType() != JsonNodeType.STRING) {
                        csv = false;
                        break;
                    }
                }
                if (!csv) {
                    continue;
                }

                ObjectNode newValue = Json.buildObjectNode();
                newValue.put("dc", dc.intValue());
                newValue.set("cos", cos);

                newCommandsObject.set(key, newValue);
            }
        }

        return newCommandsObject.isEmpty(null) ? null : newCommandsObject;
    }

}
