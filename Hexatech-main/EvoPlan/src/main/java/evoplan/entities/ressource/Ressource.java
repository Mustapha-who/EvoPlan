package evoplan.entities.ressource;

import java.io.Serializable;

public class Ressource implements Serializable {
    private int id;
    private String name;
    private String type;
    private boolean available;

    // Constructeur sans id
    public Ressource(String name, String type, boolean available) {
        this.name = name;
        this.type = type;
        this.available = available;
    }

    // Constructeur avec id
    public Ressource(int id, String name, String type, boolean available) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.available = available;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", available=" + available +
                '}';
    }
}