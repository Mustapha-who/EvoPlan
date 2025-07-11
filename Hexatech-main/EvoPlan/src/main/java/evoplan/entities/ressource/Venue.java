package evoplan.entities.ressource;

public class Venue extends Ressource {
    private String address;
    private int capacity;

    // Constructeur avec 5 arguments (sans id)
    public Venue(String name, String type, boolean availability, String address, int capacity) {
        super(name, type, availability); // Appelle le constructeur de la classe parente (Ressource)
        this.address = address;
        this.capacity = capacity;
    }

    // Constructeur avec 6 arguments (avec id)
    public Venue(int id, String name, String type, boolean availability, String address, int capacity) {
        super(id, name, type, availability); // Appelle le constructeur de la classe parente (Ressource)
        this.address = address;
        this.capacity = capacity;
    }

    // Getters et Setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return super.toString() + " | Address: " + address + " | Capacity: " + capacity;
    }
}