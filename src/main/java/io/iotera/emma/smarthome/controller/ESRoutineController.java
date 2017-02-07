package io.iotera.emma.smarthome.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.iotera.emma.smarthome.model.routine.ESRoutine;
import io.iotera.emma.smarthome.repository.ESRoutineRepository;
import io.iotera.emma.smarthome.routine.RoutineManager;
import io.iotera.emma.smarthome.utility.RoutineUtility;
import io.iotera.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public class ESRoutineController extends ESBaseController {

    @Autowired
    RoutineManager routineManager;

    @Autowired
    ESRoutineRepository routineRepository;

    @Autowired
    ESRoutineRepository.ESRoutineJpaRepository routineJpaRepository;

    protected ResponseEntity listAll(long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        ArrayNode routineArray = Json.buildArrayNode();
        List<ESRoutine> routines = routineRepository.listByAccountId(accountId);
        for (ESRoutine routine : routines) {
            ObjectNode routineObject = Json.buildObjectNode();
            routineObject.put("id", routine.getId());
            routineObject.put("name", routine.getName());
            routineObject.put("category", routine.getCategory());
            routineObject.put("trigger", routine.getTrigger());
            routineObject.set("days_of_week", Json.parseToArrayNode(routine.getDaysOfWeek()));
            routineObject.put("info", routine.getInfo());
            routineObject.set("commands", Json.parseToObjectNode(routine.getCommands()));
            routineObject.set("clients", Json.parseToArrayNode(routine.getClients()));
            routineObject.put("active", routine.isActive());
            routineObject.put("last_executed", formatDate(routine.getLastExecuted()));
            routineObject.set("last_executed_commands", Json.parseToObjectNode(routine.getLastExecutedCommands()));
            routineObject.put("last_succeeded", formatDate(routine.getLastSucceeded()));
            routineObject.put("parent", routine.getParent());
            routineArray.add(routineObject);
        }

        response.set("routines", routineArray);
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity activate(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // UPDATE
        String routineId = rget(body, "esroutine");

        ESRoutine routine = routineRepository.findByRoutineId(routineId, accountId);
        if (routine == null) {
            return okJsonFailed(-1, "routine_not_found");
        }

        response.put("id", routine.getId());
        response.put("name", routine.getName());

        routine.setActive(!routine.isActive());
        routineJpaRepository.saveAndFlush(routine);

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

        response.put("active", routine.isActive());
        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

    protected ResponseEntity read(String routineId, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();
        ESRoutine routine = routineRepository.findByRoutineId(routineId, accountId);
        if (routine == null) {
            return notFound("routine (" + routineId + ") not found");
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

    protected ResponseEntity delete(ObjectNode body, long accountId) {

        // Response
        ObjectNode response = Json.buildObjectNode();

        // DELETE
        String routineId = rget(body, "esroutine");

        ESRoutine routine = routineRepository.findByRoutineId(routineId, accountId);
        if (routine == null) {
            return okJsonFailed(-1, "routine_not_found");
        }

        response.put("id", routine.getId());
        response.put("name", routine.getName());

        // Delete child
        Date now = new Date();

        routine.setDeleted(true);
        routine.setDeletedTime(now);
        routine.setActive(false);
        routineManager.removeSchedule(accountId, routineId);

        routineJpaRepository.saveAndFlush(routine);

        response.put("status_code", 0);
        response.put("status", "success");

        // Result
        return okJson(response);
    }

}
