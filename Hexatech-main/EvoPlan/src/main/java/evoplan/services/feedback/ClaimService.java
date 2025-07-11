package evoplan.services.feedback;

import evoplan.main.DatabaseConnection;
import evoplan.entities.feedback.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimService {
    private Connection conn;

    // Constructeur pour récupérer la connexion
    public ClaimService() {
        try {
            conn = DatabaseConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    // Ajouter une réclamation
    public void addClaim(Claim claim) {
        // Generate a unique ID (using timestamp + random number)
        String uniqueId = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        claim.setId(uniqueId);

        String query = "INSERT INTO claim (id, description, claim_type, creation_date, claim_status, client_id, event_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, claim.getId());
            pstmt.setString(2, claim.getDescription());
            pstmt.setString(3, claim.getClaimType().name());
            pstmt.setDate(4, new java.sql.Date(claim.getCreationDate().getTime()));
            pstmt.setString(5, claim.getClaimStatus().name());
            pstmt.setInt(6, claim.getClientId());
            if (claim.getEventId() != null) {
                pstmt.setInt(7, claim.getEventId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Claim added successfully!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error adding claim: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Récupérer toutes les réclamations
    public List<Claim> getAllClaims() {
        List<Claim> claims = new ArrayList<>();
        String query = "SELECT * FROM claim";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                try {
                    Claim.ClaimType type;
                    String dbType = rs.getString("claim_type").toUpperCase();
                    try {
                        type = Claim.ClaimType.valueOf(dbType);
                    } catch (IllegalArgumentException e) {
                        // If the old type doesn't match new enum values, default to OTHER
                        type = Claim.ClaimType.OTHER;
                    }

                    Claim.ClaimStatus status;
                    String dbStatus = rs.getString("claim_status").toUpperCase();
                    try {
                        status = Claim.ClaimStatus.valueOf(dbStatus);
                    } catch (IllegalArgumentException e) {
                        // If the old status doesn't match new enum values, default to PENDING
                        status = Claim.ClaimStatus.PENDING;
                    }

                    claims.add(new Claim(
                            rs.getString("id"),
                            rs.getString("description"),
                            type,
                            rs.getDate("creation_date"),
                            status,
                            rs.getInt("client_id"),
                            rs.getInt("event_id")
                    ));
                } catch (Exception e) {
                    System.out.println("❌ Erreur lors de la conversion d'une réclamation : " + e.getMessage());
                    // Continue to next record if there's an error with current one
                    continue;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
        }
        return claims;
    }

    // Supprimer une réclamation
    public void deleteClaim(String id) {
        String query = "DELETE FROM claim WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✅ Réclamation supprimée !");
            else System.out.println("⚠️ Réclamation non trouvée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // Mettre à jour une réclamation
    public void updateClaim(Claim claim) {
        String query = "UPDATE claim SET description = ?, claim_type = ?, claim_status = ?, client_id = ?, event_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, claim.getDescription());
            pstmt.setString(2, claim.getClaimType().name());
            pstmt.setString(3, claim.getClaimStatus().name());
            pstmt.setInt(4, claim.getClientId());
            if (claim.getEventId() != null) {
                pstmt.setInt(5, claim.getEventId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setString(6, claim.getId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✅ Réclamation mise à jour !");
            else System.out.println("⚠️ Réclamation non trouvée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }
}
