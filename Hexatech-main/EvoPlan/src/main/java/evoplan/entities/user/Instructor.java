package evoplan.entities.user;

import java.util.Objects;

public class Instructor extends User {
    private String certification;
    private boolean isApproved = false;

    //Mustapha: I added this Constructor to retreive the id from table instructor since workshop and instructor are linked
    public Instructor(int id, String certification, boolean isApproved) {
        super(id);
        this.certification = certification;
        this.isApproved = isApproved;
    }


    public Instructor(String certification, boolean isApproved) {
        this.certification = certification;
        this.isApproved = isApproved;
    }

    public Instructor(int id, String email, String password,String name, String certification, boolean isApproved) {
        super(id, email, password,name);
        this.certification = certification;
        this.isApproved = isApproved;
    }

    public Instructor(String email, String password,String name, String certification, boolean isApproved) {
        super(email, password,name);
        this.certification = certification;
        this.isApproved = isApproved;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Instructor that = (Instructor) o;
        return isApproved == that.isApproved && Objects.equals(certification, that.certification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), certification, isApproved);
    }

    @Override
    public String toString() {
        return "Instructor{" +
                "certification='" + certification + '\'' +
                ", isApproved=" + isApproved +
                '}';
    }
}

