package evoplan.entities.user;

import java.util.Objects;

public class EventPlanner extends User {
    private String specialization;
    private EventPlannerModule assignedModule;

    public EventPlanner() {

    }
    public EventPlanner(String specialization, EventPlannerModule assignedModule) {
        this.specialization = specialization;
        this.assignedModule = assignedModule;

    }

    public EventPlanner(String email, String password, String name, String specialization, EventPlannerModule assignedModule) {
        super(email, password, name);
        this.specialization = specialization;
        this.assignedModule = assignedModule;
    }

    public EventPlanner(int id, String email, String password, String name, String specialization, EventPlannerModule assignedModule) {
        super(id, email, password, name);
        this.specialization = specialization;
        this.assignedModule = assignedModule;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public EventPlannerModule getAssignedModule() {
        return assignedModule;
    }

    public void setAssignedModule(EventPlannerModule assignedModule) {
        this.assignedModule = assignedModule;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EventPlanner that = (EventPlanner) o;
        return Objects.equals(specialization, that.specialization) && assignedModule == that.assignedModule;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), specialization, assignedModule);
    }

    @Override
    public String toString() {
        return "EventPlanner{" +
                "specialization='" + specialization + '\'' +
                ", assignedModule=" + assignedModule +
                '}';
    }
}
