package evoplan.entities;

public class Partner {

    private int id_partner;
    private PartnerType type_partner;
    private String email;
    private String phone_Number;
    private String logo;

    // Constructor

    public Partner(int id_partner, PartnerType type_partner, String email, String phone_Number, String logo) {


        this.id_partner=id_partner;
        this.type_partner = type_partner;
        this.email = email;
        this.phone_Number = phone_Number;
        this.logo = logo;
    }

    public Partner(PartnerType type_partner, String email, String phone_Number, String logo) {


        this.type_partner = type_partner;
        this.email = email;
        this.phone_Number = phone_Number;
        this.logo = logo;
    }
    public Partner(String logo) {

        this.logo = logo;
    }

    // Empty constructor
    public Partner() {}

    // Getters and Setters
    public int getId_partner() {
        return id_partner;
    }

    public void setId_partner(int id_partner) {
        this.id_partner = id_partner;
    }


    public PartnerType getType_partner() {
        return type_partner;
    }

    public void setType_partner(PartnerType type_partner) {
        this.type_partner = type_partner;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_Number() {
        return phone_Number;
    }

    public void setPhone_Number(String phone_Number) {
        this.phone_Number = phone_Number;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "Partner{" +
                "id_partner=" + id_partner +
                ", type_partner='" + type_partner + '\'' +
                ", email='" + email + '\'' +
                ", phone_Number='" + phone_Number + '\'' +
                ", logo='" + logo + '\'' +
                '}';
    }
}