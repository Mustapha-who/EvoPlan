package evoplan.entities.ressource;
import java.lang.String;

public class Equipment extends Ressource {
    private String equipmentType;
    private int quantity;

    // Constructeur sans id
    public Equipment(String name, String type, boolean availability, String equipmentType, int quantity) {
        super(name, type, availability); // Appelle le constructeur de la classe parente (Ressource)
        this.equipmentType = equipmentType;
        this.quantity = quantity;
    }

    // Constructeur avec id
    public Equipment(int id, String name, String type, boolean availability, String equipmentType, int quantity) {
        super(id, name, type, availability); // Appelle le constructeur de la classe parente (Ressource)
        this.equipmentType = equipmentType;
        this.quantity = quantity;
    }

    // Getters et Setters
    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return super.toString() + " | Equipment Type: " + equipmentType + " | Quantity: " + quantity;
    }
}