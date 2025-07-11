package evoplan.entities.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private int idEvent;
    private String nom;
    private String description;// Nouvelle description
    private LocalDateTime dateDebut; // Date et heure
    private LocalDateTime dateFin;   // Date et heure
    private Regions lieu; // Utilisation de l'énumération Regions
    private int capacite;
    private double prix;
    private TypeStatus statut;
    private String imageEvent;
    private int nombre_visites;// Image de l'événement

    public Event() {}

    //mustapha: I added this so I can use it in the frontend and display the data for my workshop
    public Event(int idEvent, String nom, String description) {
        this.idEvent = idEvent;
        this.nom = nom;
        this.description = description;
    }

    public Event(int idEvent, String nom, int capacite,TypeStatus statut) {
        this.idEvent = idEvent;
        this.nom = nom;
        this.capacite = capacite;
        this.statut = statut;
    }
    public Event(int idEvent, String nom, String description, LocalDateTime dateDebut, LocalDateTime dateFin, Regions lieu, int capacite, double prix, TypeStatus statut, String imageEvent) {
        this.idEvent = idEvent;
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.capacite = capacite;
        this.prix = prix;
        this.statut = statut;
        this.imageEvent = imageEvent;
    }

    public Event(String nom, String description, LocalDateTime dateDebut, LocalDateTime dateFin, Regions lieu, int capacite, double prix, TypeStatus statut, String imageEvent) {
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.capacite = capacite;
        this.prix = prix;
        this.statut = statut;
        this.imageEvent = imageEvent;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Regions getLieu() {
        return lieu;
    }

    public void setLieu(Regions lieu) {
        this.lieu = lieu;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public TypeStatus getStatut() {
        return statut;
    }

    public void setStatut(TypeStatus statut) {
        this.statut = statut;
    }

    public String getImageEvent() {
        return imageEvent;
    }

    public void setImageEvent(String imageEvent) {
        this.imageEvent = imageEvent;
    }
    public void setNombre_visites(int nombre_visites) {
        this.nombre_visites = nombre_visites;
    }
    public int getNombre_visites() {
        return nombre_visites;
    }
    // Formatage correct de la date pour l'affichage
    public String getFormattedDateDebut() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateDebut.format(formatter);
    }

    public String getFormattedDateFin() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateFin.format(formatter);
    }
}
