package evoplan.entities;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Partnership  {
    private int id_partnership;
    private int id_partner;
    private int id_event;
    private String date_debut;
    private String date_fin;
    private String terms;

    // Constructors
    public Partnership() {}

    // Constructor without logo
    public Partnership(int id_partner, int id_event, String date_debut, String date_fin, String terms) {
        this.id_partner = id_partner;
        this.id_event = id_event;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.terms = terms;
    }

    public Partnership(int id_partnership, int id_partner, int id_event, String date_debut, String date_fin, String terms) {
        this.id_partnership = id_partnership;
        this.id_partner = id_partner;
        this.id_event = id_event;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.terms = terms;
    }

    // Getters & Setters
    public int getId_partnership() {
        return id_partnership;
    }

    public void setId_partnership(int id_partnership) {
        this.id_partnership = id_partnership;
    }

    public int getId_partner() {
        return id_partner;
    }

    public void setId_partner(int id_partner) {
        this.id_partner = id_partner;
    }

    public int getId_event() {
        return id_event;
    }

    public void setId_event(int id_event) {
        this.id_event = id_event;
    }

    public String getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(String date_debut) {
        this.date_debut = date_debut;
    }

    public String getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(String date_fin) {
        this.date_fin = date_fin;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }



    @Override
    public String toString() {
        return "Partnership{" +
                "id_partnership=" + id_partnership +
                ", id_partner=" + id_partner +
                ", id_event=" + id_event +
                ", date_debut='" + date_debut + '\'' +
                ", date_fin='" + date_fin + '\'' +
                ", terms='" + terms + '\'' +
                '}';
    }


}

