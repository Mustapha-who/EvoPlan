package evoplan.services.Partner;

import evoplan.entities.*;



import evoplan.entities.Partner;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerService implements Ipartner<Partner> {

    private Connection cnx;

    public PartnerService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    // ➤ Add a new Partner
    @Override
    public void ajouter(Partner p) {
        String req = "INSERT INTO partner (type_partner, email, phone_number, logo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, p.getType_partner().name());
            stm.setString(2, p.getEmail());
            stm.setString(3, p.getPhone_Number());
            stm.setString(4, p.getLogo());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding Partner: " + e.getMessage());
        }
    }

    // ➤ Update an existing Partner
    @Override
    public void modifier(Partner p) {
        String req = "UPDATE partner SET type_partner = ?, email = ?, phone_number = ?, logo = ? WHERE id_partner = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, p.getType_partner().name());
            stm.setString(2, p.getEmail());
            stm.setString(3, p.getPhone_Number());
            stm.setString(4, p.getLogo());
            stm.setInt(5, p.getId_partner());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Partner: " + e.getMessage());
        }
    }

    // ➤ Delete a Partner by ID
    @Override
    public void supprimer(Partner p) {
        // First, delete any contracts associated with the partner
        deleteContractsByPartnerId(p.getId_partner());

        // Then delete any partnerships associated with the partner
        deletePartnershipsByPartnerId(p.getId_partner());

        // Now delete the partner
        String req = "DELETE FROM partner WHERE id_partner = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, p.getId_partner());
            stm.executeUpdate();
            System.out.println("✅ Partner deleted successfully with ID: " + p.getId_partner());
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Partner: " + e.getMessage());
        }
    }
    public String partnerExists(String email, String phoneNumber, String logo) {
        String req = "SELECT COUNT(*) FROM partner WHERE email = ? OR phone_number = ? OR logo = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, email);
            stm.setString(2, phoneNumber);
            stm.setString(3, logo);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    // Check which field exists
                    if (emailExists(email)) {
                        return "email"; // Email exists
                    }
                    if (phoneNumberExists(phoneNumber)) {
                        return "phone"; // Phone number exists
                    }
                    if (logoExists(logo)) {
                        return "logo"; // Logo exists
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking partner existence: " + e.getMessage());
        }
        return null; // Return null if no partner exists
    }

    // Helper methods to check existence of each field
    private boolean emailExists(String email) {
        String req = "SELECT COUNT(*) FROM partner WHERE email = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, email);
            ResultSet rs = stm.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence: " + e.getMessage());
        }
    }

    private boolean phoneNumberExists(String phoneNumber) {
        String req = "SELECT COUNT(*) FROM partner WHERE phone_number = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, phoneNumber);
            ResultSet rs = stm.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking phone number existence: " + e.getMessage());
        }
    }

    private boolean logoExists(String logo) {
        String req = "SELECT COUNT(*) FROM partner WHERE logo = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, logo);
            ResultSet rs = stm.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking logo existence: " + e.getMessage());
        }
    }
    private void deletePartnershipsByPartnerId(int partnerId) {
        String req = "DELETE FROM partnership WHERE id_partner = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnerId);
            int deletedRows = stm.executeUpdate();
            System.out.println("✅ Related partnerships deleted successfully for partner ID: " + partnerId + ", Rows affected: " + deletedRows);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting partnerships for Partner: " + e.getMessage());
        }
    }
    private void deleteContractsByPartnerId(int partnerId) {
        String req = "DELETE FROM contract WHERE id_partner = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnerId);
            int deletedRows = stm.executeUpdate();
            System.out.println("✅ Related contracts deleted successfully for partner ID: " + partnerId + ", Rows affected: " + deletedRows);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting contracts for Partner: " + e.getMessage());
        }
    }

    // ➤ Retrieve all partners
    @Override
    public List<Partner> getall() {
        List<Partner> partners = new ArrayList<>();
        String req = "SELECT * FROM partner";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Partner p = new Partner();
                p.setId_partner(rs.getInt("id_partner"));
                p.setType_partner(PartnerType.valueOf(rs.getString("type_partner")));
                p.setEmail(rs.getString("email"));
                p.setPhone_Number(rs.getString("phone_number"));
                p.setLogo(rs.getString("logo"));
                partners.add(p);
            }

            System.out.println(partners);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving partners: " + e.getMessage());
        }

        return partners;
    }


    // ➤ Retrieve one Partner by ID
    @Override
    public Partner getone() {
        return null;
    }

    @Override
    public List<Partner> getPartnersByEvent(int eventId) {
        List<Partner> partners = new ArrayList<>();
        String query = "SELECT p.logo " +
                "FROM partner p " +
                "INNER JOIN partnership ps ON p.id_partner = ps.id_partner " +
                "WHERE ps.id_event = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                /*PartnerType type = PartnerType.valueOf(rs.getString("type_partner"));*/ // Conversion en Enum
                String logo = rs.getString("logo");

                partners.add(new Partner(logo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return partners;
    }

}