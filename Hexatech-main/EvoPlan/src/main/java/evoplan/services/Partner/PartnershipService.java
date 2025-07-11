package evoplan.services.Partner;

import evoplan.entities.Partner;
import evoplan.entities.PartnerType;
import evoplan.entities.Partnership;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnershipService implements IPartnership<Partnership> {
    private Connection cnx;

    public PartnershipService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    // ➤ Add a new partnership
    @Override
    public void ajouter(Partnership p) {
        String req = "INSERT INTO partnership (id_partner, id_event, date_debut, date_fin, terms) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stm = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            stm.setInt(1, p.getId_partner());
            stm.setInt(2, p.getId_event());
            stm.setString(3, p.getDate_debut());
            stm.setString(4, p.getDate_fin());
            stm.setString(5, p.getTerms());
            stm.executeUpdate();

            // Get the generated partnership ID
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next()) {
                int partnershipId = rs.getInt(1);
                // Create corresponding contract
                createContract(partnershipId, p);
            }

            System.out.println("✅ Partnership added successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error adding Partnership: " + e.getMessage());
        }
    }
    public Partner getPartnerById(int idPartner) {
        String req = "SELECT * FROM partner WHERE id_partner = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, idPartner);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                Partner partner = new Partner();
                partner.setId_partner(rs.getInt("id_partner"));
                partner.setType_partner(PartnerType.valueOf(rs.getString("type_partner")));
                partner.setEmail(rs.getString("email"));
                partner.setPhone_Number(rs.getString("phone_number"));
                partner.setLogo(rs.getString("logo"));
                return partner;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving partner by ID: " + e.getMessage());
        }
        return null; // Return null if no partner is found
    }

    private void createContract(int partnershipId, Partnership p) {
        String req = "INSERT INTO contract (id_partnership, id_partner, date_debut, date_fin, terms, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnershipId);
            stm.setInt(2, p.getId_partner());
            stm.setString(3, p.getDate_debut());
            stm.setString(4, p.getDate_fin());
            stm.setString(5, p.getTerms());
            stm.setString(6, "active");
            stm.executeUpdate();
            System.out.println("✅ Contract created successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error creating Contract: " + e.getMessage());
        }
    }
    public boolean partnershipExists(int partnerId, int eventId) {
        String query = "SELECT COUNT(*) FROM partnership WHERE id_partner = ? AND id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, partnerId);
            ps.setInt(2, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if count is greater than 0
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking partnership existence: " + e.getMessage());
        }
        return false; // Default to false if an error occurs
    }

    // ➤ Update an existing partnership
    @Override
    public void modifier(Partnership p) {
        String req = "UPDATE partnership SET id_partner = ?, id_event = ?, date_debut = ?, date_fin = ?, terms = ? WHERE id_partnership = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, p.getId_partner());
            stm.setInt(2, p.getId_event());
            stm.setString(3, p.getDate_debut());
            stm.setString(4, p.getDate_fin());
            stm.setString(5, p.getTerms());
            stm.setInt(6, p.getId_partnership());
            stm.executeUpdate();
            System.out.println("✅ Partnership updated successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Partnership: " + e.getMessage());
        }
    }

    // ➤ Delete a partnership
    @Override
    public void supprimer(Partnership p) {
        // First, delete related contracts
        deleteContractsByPartnershipId(p.getId_partnership());

        // Then, delete the partnership
        String req = "DELETE FROM partnership WHERE id_partnership = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, p.getId_partnership());
            stm.executeUpdate();
            System.out.println("✅ Partnership deleted successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Partnership: " + e.getMessage());
        }
    }

    private void deleteContractsByPartnershipId(int partnershipId) {
        String req = "DELETE FROM contract WHERE id_partnership = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnershipId);
            stm.executeUpdate();
            System.out.println("✅ Related contracts deleted successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting related contracts: " + e.getMessage());
        }
    }

    // ➤ Retrieve all partnerships
    @Override
    public List<Partnership> getAll() {
        List<Partnership> partnerships = new ArrayList<>();
        String req = "SELECT * FROM partnership";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                partnerships.add(createPartnershipFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving partnerships: " + e.getMessage());
        }

        return partnerships;
    }

    public List<Partnership> getPartnershipsByPartnerId(int partnerId) {
        List<Partnership> partnerships = new ArrayList<>();
        String req = "SELECT * FROM partnership WHERE id_partner = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnerId);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                partnerships.add(createPartnershipFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving partnerships for partner: " + e.getMessage());
        }

        return partnerships;
    }

    // Helper method to create Partnership object from ResultSet
    private Partnership createPartnershipFromResultSet(ResultSet rs) throws SQLException {
        return new Partnership(
                rs.getInt("id_partnership"),
                rs.getInt("id_partner"),
                rs.getInt("id_event"),
                rs.getString("date_debut"),
                rs.getString("date_fin"),
                rs.getString("terms")
        );
    }

    // ➤ Retrieve one partnership by ID
    @Override
    public Partnership getOne(int id) {
        Partnership p = null;
        String req = "SELECT * FROM partnership WHERE id_partnership = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                p = new Partnership(
                        rs.getInt("id_partnership"),
                        rs.getInt("id_partner"),
                        rs.getInt("id_event"),
                        rs.getString("date_debut"),
                        rs.getString("date_fin"),
                        rs.getString("terms")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving partnership: " + e.getMessage());
        }
        return p;
    }

    public void updatePartnership(Partnership partnership) {
        // Update the partnership
        String updatePartnershipQuery = "UPDATE partnership SET terms = ?, date_debut = ?, date_fin = ? WHERE id_partnership = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(updatePartnershipQuery)) {
            stmt.setString(1, partnership.getTerms());
            stmt.setString(2, partnership.getDate_debut());
            stmt.setString(3, partnership.getDate_fin());
            stmt.setInt(4, partnership.getId_partnership());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Partnership updated, rows affected: " + rowsAffected);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating partnership: " + e.getMessage());
        }

        // Update related contracts if necessary
        updateRelatedContracts(partnership);
    }

    private void updateRelatedContracts(Partnership partnership) {
        String updateContractQuery = "UPDATE contract SET terms = ? WHERE id_partnership = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(updateContractQuery)) {
            stmt.setString(1, partnership.getTerms()); // Assuming terms are relevant
            stmt.setInt(2, partnership.getId_partnership());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Contracts updated, rows affected: " + rowsAffected);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating contracts related to partnership: " + e.getMessage());
        }
    }
    /*public List<Partnership> getPartnershipsForEvent(int eventId) {
        List<Partnership> partnerships = new ArrayList<>();
        String query = """
            SELECT p.*, pr.* 
            FROM partnership p 
            JOIN partner pr ON p.id_partner = pr.id_partner 
            WHERE p.id_event = ?
        """;

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Partnership partnership = new Partnership();
                partnership.setId_partnership(rs.getInt("id_partnership"));
                partnership.setDate_debut(rs.getString("date_debut"));
                partnership.setDate_fin(rs.getString("date_fin"));
                partnership.setTerms(rs.getString("terms"));

                Partner partner = new Partner();
                partner.setId_partner(rs.getInt("id_partner"));
                partner.setType_partner(PartnerType.valueOf(rs.getString("type_partner")));
                partner.setEmail(rs.getString("email"));
                partner.setPhone_Number(rs.getString("phone_Number"));
                partner.setLogo(rs.getString("logo"));

                partnership.setPartner(partner);
                partnerships.add(partnership);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return partnerships;
    }*/

}

