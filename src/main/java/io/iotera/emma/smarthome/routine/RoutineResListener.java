package io.iotera.emma.smarthome.routine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.repository.ESDeviceRepository;
import io.iotera.emma.smarthome.repository.ESRoutineRepository;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class RoutineResListener implements ApplicationListener<RoutineResEvent> {

    @Autowired
    ESDeviceRepository deviceRepository;

    @Autowired
    ESRoutineRepository routineRepository;

    @Override
    public void onApplicationEvent(RoutineResEvent event) {

        long accountId = event.getAccountId();
        ObjectNode payload = event.getPayload();

        if (!payload.has("rid") || !payload.has("st") || !payload.has("ec")) {
            return;
        }
        String routineId = payload.get("rid").textValue().trim();
        JsonNode successNode = payload.get("st");

        if (!successNode.isBoolean()) {
            return;
        }

        boolean success = successNode.booleanValue();
        String sc = payload.get("ec").toString();

        ObjectNode succeededCommands = Json.parseToObjectNode(sc);
        if (succeededCommands == null) {
            return;
        }

        ObjectNode newCommandsObject = Json.buildObjectNode();
        Iterator<Map.Entry<String, JsonNode>> i$ = succeededCommands.fields();

        while (i$.hasNext()) {
            Map.Entry<String, JsonNode> en = i$.next();
            String key = en.getKey();
            JsonNode value = en.getValue();

            if (!value.isObject()) {
                continue;
            }

            if (value.has("dc") && value.has("cs")) {
                JsonNode dc = value.get("dc");
                if (dc.getNodeType() != JsonNodeType.NUMBER) {
                    continue;
                }
                JsonNode cs = value.get("cs");
                if (!cs.isArray()) {
                    continue;
                }

                boolean csv = true;
                for (JsonNode cso : cs) {
                    if (cso.getNodeType() != JsonNodeType.STRING) {
                        csv = false;
                        break;
                    } else {
                        String c = cso.textValue().trim();
                        deviceRepository.updateStatus(key, dc.asInt(-1), c, accountId);
                    }
                }

                if (!csv) {
                    continue;
                }

                ObjectNode newValue = Json.buildObjectNode();
                newValue.put("dc", dc.asInt(-1));
                newValue.set("cs", cs);

                newCommandsObject.set(key, newValue);
            }
        }

        if (!newCommandsObject.isEmpty(null))

        {
            routineRepository.updateSuccess(routineId, success,
                    Json.toStringIgnoreNull(succeededCommands), accountId);
        }
    }

}
