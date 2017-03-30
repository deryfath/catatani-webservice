package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.preference.RoutinePref;
import io.iotera.emma.smarthome.repository.ESRoutineRepo;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.util.RoutineUtility;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class ESScheduleController extends ESRoutineController {

    @Autowired
    RoutineManager routineManager;

    @Autowired
    ESRoutineRepo routineRepo;

    @Autowired
    ESRoutineRepo.ESRoutineJRepo routineJRepo;

    protected ResponseEntity create(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        String name = rget(body, "esname");
        String trigger = rget(body, "estrigger");
        ArrayNode daysOfWeek = rget(body, "esdays", ArrayNode.class);
        String daysString = Json.toStringIgnoreNull(daysOfWeek);
        String cronExpression = RoutineUtility.getCronExpression(daysString, trigger);
        if (cronExpression == null) {
            return badRequest("wrong time format");
        }
        ObjectNode commands = rget(body, "escommands", ObjectNode.class);
        String commandsString = Json.toStringIgnoreNull(commands);
        ObjectNode newCommands = RoutineUtility.getValidCommands(commandsString);
        if (newCommands == null) {
            return badRequest("wrong commands format");
        }
        commandsString = Json.toStringIgnoreNull(newCommands);

        if (!routineRepo.findByName(name, accountId).isEmpty()) {
            return okJsonFailed(-2, "routine_name_not_available");
        }

        ESRoutine routine = new ESRoutine(name, RoutinePref.CAT_SCHEDULE,
                trigger, daysString, null, commandsString, null, accountId);

        routineJRepo.saveAndFlush(routine);

        if (routine.isActive()) {
            routineManager.updateSchedule(accountId, routine, cronExpression);
        }

        response.put("id", routine.getId());
        response.put("name", routine.getName());
        response.put("category", routine.getCategory());
        response.put("trigger", routine.getTrigger());
        response.set("days_of_week", Json.parseToArrayNode(routine.getDaysOfWeek()));
        response.put("info", routine.getInfo());
        response.set("commands", Json.parseToObjectNode(routine.getCommands()));
        response.set("clients", Json.parseToArrayNode(routine.getClients()));
        response.put("active", routine.isActive());
        response.put("last_executed", formatDate(routine.getLastExecuted()));
        response.set("last_executed_commands", Json.parseToObjectNode(routine.getLastExecutedCommands()));
        response.put("last_succeeded", formatDate(routine.getLastSucceeded()));
        response.put("parent", routine.getParent());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity update(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // UPDATE
        boolean edit = false;
        String routineId = rget(body, "esroutine");

        ESRoutine routine = routineRepo.findByRoutineId(routineId, accountId);
        if (routine == null) {
            return okJsonFailed(-1, "routine_not_found");
        }

        response.put("id", routine.getId());
        response.put("name", routine.getName());

        if (has(body, "esname")) {
            String name = get(body, "esname");
            // Check room name
            if (!routine.getName().equals(name)) {
                if (!routineRepo.findByName(name, accountId).isEmpty()) {
                    return okJsonFailed(-2, "routine_name_not_available");
                }
                routine.setName(name);
                edit = true;
            }
            response.put("name", name);
        }

        if (has(body, "estrigger")) {
            String trigger = get(body, "estrigger");
            if (!routine.getTrigger().equals(trigger)) {
                String cronExpression = RoutineUtility.getCronExpression(routine.getDaysOfWeek(), trigger);
                if (cronExpression == null) {
                    return badRequest("wrong time format");
                }
                routine.setTrigger(trigger);
                edit = true;
            }
            response.put("trigger", trigger);
        }

        if (has(body, "esdays")) {
            ArrayNode daysOfWeek = get(body, "esdays", ArrayNode.class);
            String daysString = daysOfWeek.toString();
            if (!routine.getDaysOfWeek().equals("esdays")) {
                String cronExpression = RoutineUtility.getCronExpression(daysString, routine.getTrigger());
                if (cronExpression == null) {
                    return badRequest("wrong time format");
                }
                routine.setDaysOfWeek(daysString);
                edit = true;
            }
            response.put("days_of_week", daysString);
        }

        if (has(body, "esinfo")) {
            String info = get(body, "esinfo");
            if (!routine.getInfo().equals(info)) {
                routine.setInfo(info);
                edit = true;
            }
            response.put("info", info);
        }

        if (has(body, "escommands")) {
            ObjectNode commands = get(body, "escommands", ObjectNode.class);
            String commandsString = Json.toStringIgnoreNull(commands);
            if (!routine.getCommands().equals(commandsString)) {
                ObjectNode newCommands = RoutineUtility.getValidCommands(commandsString);
                if (newCommands == null) {
                    return badRequest("wrong commands format");
                }
                commandsString = Json.toStringIgnoreNull(newCommands);
                routine.setCommands(commandsString);
                edit = true;
            }
            response.put("commands", commandsString);
        }

        if (has(body, "esactive")) {
            boolean active = get(body, "esactive", Boolean.class);
            if (routine.isActive() != active) {
                routine.setActive(active);
                edit = true;
            }
            response.put("active", active);
        }

        if (edit) {
            routineJRepo.saveAndFlush(routine);

            if (routine.isActive()) {
                String cronExpression = RoutineUtility.getCronExpression(routine.getDaysOfWeek(),
                        routine.getTrigger());
                boolean valid = RoutineUtility.getValidCommands(routine.getCommands()) != null;
                if (cronExpression != null && valid) {
                    routineManager.updateSchedule(accountId, routine, cronExpression);
                }
            } else {
                routineManager.removeSchedule(accountId, routine.getId());
            }
        }

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
