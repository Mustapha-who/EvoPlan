package evoplan.services.Partner;

import evoplan.entities.Contract;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContractService implements IContract<Contract> {
    private Connection cnx;
    private static final List<String> VALID_STATUSES = Arrays.asList(
            "active", "expired", "terminated", "cancelled"
    );

    public ContractService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void ajouter(Contract c) {
        String req = "INSERT INTO contract (id_partnership, id_partner, date_debut, date_fin, terms, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, c.getId_partnership());
            stm.setInt(2, c.getId_partner());
            stm.setString(3, c.getDate_debut());
            stm.setString(4, c.getDate_fin());
            stm.setString(5, c.getTerms());
            stm.setString(6, c.getStatus());
            stm.executeUpdate();
            System.out.println("✅ Contract added successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error adding Contract: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Contract c) {
        String req = "UPDATE contract SET id_partnership = ?, id_partner = ?, date_debut = ?, date_fin = ?, terms = ?, status = ? WHERE id_contract = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, c.getId_partnership());
            stm.setInt(2, c.getId_partner());
            stm.setString(3, c.getDate_debut());
            stm.setString(4, c.getDate_fin());
            stm.setString(5, c.getTerms());
            stm.setString(6, c.getStatus());
            stm.setInt(7, c.getId_contract());
            stm.executeUpdate();
            System.out.println("✅ Contract updated successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Contract: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Contract c) {
        String req = "DELETE FROM contract WHERE id_contract = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, c.getId_contract());
            stm.executeUpdate();
            System.out.println("✅ Contract deleted successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Contract: " + e.getMessage());
        }
    }

    @Override
    public List<Contract> getAll() {
        List<Contract> contracts = new ArrayList<>();
        String req = "SELECT * FROM contract";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                contracts.add(createContractFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving contracts: " + e.getMessage());
        }

        return contracts;
    }

    @Override
    public Contract getOne(int id) {
        String req = "SELECT * FROM contract WHERE id_contract = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                return createContractFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving contract: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Contract> getContractsByPartnerId(int partnerId) {
        List<Contract> contracts = new ArrayList<>();
        String req = "SELECT * FROM contract WHERE id_partner = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnerId);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                contracts.add(createContractFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving contracts for partner: " + e.getMessage());
        }

        return contracts;
    }

    @Override
    public List<Contract> getContractsByPartnershipId(int partnershipId) {
        List<Contract> contracts = new ArrayList<>();
        String req = "SELECT * FROM contract WHERE id_partnership = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, partnershipId);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                contracts.add(createContractFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving contracts for partnership: " + e.getMessage());
        }

        return contracts;
    }

    @Override
    public void updateStatus(int contractId, String newStatus) {
        if (!VALID_STATUSES.contains(newStatus.toLowerCase())) {
            throw new IllegalArgumentException("Invalid status: " + newStatus + ". Valid statuses are: " + VALID_STATUSES);
        }

        String req = "UPDATE contract SET status = ? WHERE id_contract = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, newStatus.toLowerCase());
            stm.setInt(2, contractId);
            stm.executeUpdate();
            System.out.println("✅ Contract status updated successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("Error updating contract status: " + e.getMessage());
        }
    }

    // Helper method to create Contract object from ResultSet
    private Contract createContractFromResultSet(ResultSet rs) throws SQLException {
        return new Contract(
                rs.getInt("id_contract"),
                rs.getInt("id_partnership"),
                rs.getInt("id_partner"),
                rs.getString("date_debut"),
                rs.getString("date_fin"),
                rs.getString("terms"),
                rs.getString("status")
        );
    }

    // Utility method to check for expired contracts and update their status
    public void checkAndUpdateExpiredContracts() {
        String req = "UPDATE contract SET status = 'expired' " +
                "WHERE status = 'active' AND date_fin < CURRENT_DATE";
        try (Statement stm = cnx.createStatement()) {
            int updated = stm.executeUpdate(req);
            if (updated > 0) {
                System.out.println("✅ Updated " + updated + " expired contracts");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating expired contracts: " + e.getMessage());
        }
    }
}