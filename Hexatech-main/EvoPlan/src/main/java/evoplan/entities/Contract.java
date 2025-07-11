package evoplan.entities;

public class Contract {
    private int id_contract;
    private int id_partnership;
    private int id_partner;
    private String date_debut;
    private String date_fin;
    private String terms;
    private String status; // e.g., "active", "terminated", "expired"

    public Contract() {}

    public Contract(int id_partnership, int id_partner, String date_debut, String date_fin, String terms, String status) {
        this.id_partnership = id_partnership;
        this.id_partner = id_partner;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.terms = terms;
        this.status = status;
    }

    // Constructor with id
    public Contract(int id_contract, int id_partnership, int id_partner, String date_debut, String date_fin, String terms, String status) {
        this.id_contract = id_contract;
        this.id_partnership = id_partnership;
        this.id_partner = id_partner;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.terms = terms;
        this.status = status;
    }

    // Getters and Setters
    public int getId_contract() {
        return id_contract;
    }

    public void setId_contract(int id_contract) {
        this.id_contract = id_contract;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id_contract=" + id_contract +
                ", id_partnership=" + id_partnership +
                ", id_partner=" + id_partner +
                ", date_debut='" + date_debut + '\'' +
                ", date_fin='" + date_fin + '\'' +
                ", terms='" + terms + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
