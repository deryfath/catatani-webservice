package io.iotera.emma.smarthome.model.routine;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = ESRoutine.NAME)
public class ESRoutine {

    public static final String NAME = "v2_routine_tbl";

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    protected String id;

    @Column(nullable = false)
    protected String name;

    @Column(nullable = false)
    protected int category;

    @Column(name = "routine_trigger", nullable = false)
    protected String trigger;

    @Column(name = "days_of_week", nullable = false)
    protected String daysOfWeek;

    @Column(length = 1023)
    protected String info;

    @Column(columnDefinition = "text", nullable = false)
    protected String commands;

    @Column(length = 2047)
    protected String clients;

    @Column(nullable = false)
    protected boolean active;

    @Column(name = "last_executed")
    protected Date lastExecuted;

    @Column(name = "last_executed_commands", columnDefinition = "text")
    protected String lastExecutedCommands;

    @Column(name = "last_succeeded")
    protected Date lastSucceeded;

    ////////////
    // Parent //
    @Column(name = "__parent__", nullable = false)
    protected String parent;

    ///////////
    // Order //
    @Column(name = "__added__", nullable = false)
    protected Date addedTime;

    @Column(name = "__order__", nullable = false)
    protected long order;

    //////////////////
    // Deleted Flag //
    @Column(name = "__deleted_flag__", nullable = false)
    protected boolean deleted;

    @Column(name = "__deleted_time__")
    protected Date deletedTime;

    /////////////////
    // Constructor //
    /////////////////

    protected ESRoutine() {
    }

    public ESRoutine(String name, int category, String trigger, String daysOfWeek, String info, String commands,
                     String clients, long hubId) {

        this.name = name;
        this.category = category;
        this.trigger = trigger;
        this.daysOfWeek = daysOfWeek;
        this.info = info;
        this.commands = commands;

        this.clients = clients;
        this.active = true;
        this.parent = parent(hubId);

        this.addedTime = new Date();
        this.order = 0;
        this.deleted = false;
    }

    /////////////////////
    // Getter & Setter //
    /////////////////////

    public static String parent(long hubId) {
        return hubId + "/";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public String getClients() {
        return clients;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getLastExecuted() {
        return lastExecuted;
    }

    public void setLastExecuted(Date lastExecuted) {
        this.lastExecuted = lastExecuted;
    }

    public String getLastExecutedCommands() {
        return lastExecutedCommands;
    }

    public void setLastExecutedCommands(String lastExecutedCommands) {
        this.lastExecutedCommands = lastExecutedCommands;
    }

    public Date getLastSucceeded() {
        return lastSucceeded;
    }

    public void setLastSucceeded(Date lastSucceeded) {
        this.lastSucceeded = lastSucceeded;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Date getAddedTime() {
        return addedTime;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeletedTime() {
        return deletedTime;
    }

    ////////////
    // Method //
    ////////////

    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    @Transient
    public long getHubId() {
        long hubId;
        try {
            hubId = Long.parseLong(parent.split("/")[0]);
            return hubId;
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }

        return -1;
    }

}
