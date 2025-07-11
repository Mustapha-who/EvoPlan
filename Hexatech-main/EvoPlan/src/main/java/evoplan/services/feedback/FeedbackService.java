package evoplan.services.feedback;

import evoplan.main.DatabaseConnection;
import evoplan.entities.feedback.Feedback;
import evoplan.entities.feedback.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService {
    private Connection conn;

    // Constructeur pour récupérer la connexion
    public FeedbackService() {
        try {
            conn = DatabaseConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    // Ajouter un feedback
    public boolean addFeedback(Feedback feedback) {
        String query = "INSERT INTO feedback (client_id, event_id, comments, rating) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, feedback.getClientId());
            if (feedback.getEventId() != null) {
                pstmt.setInt(2, feedback.getEventId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, feedback.getComments());
            pstmt.setInt(4, feedback.getRating());
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        feedback.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Feedback added successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error adding feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Récupérer tous les feedbacks
    public List<Feedback> getAllFeedbacks() {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM feedback";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                feedbacks.add(new Feedback(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getInt("event_id"),
                        rs.getString("comments"),
                        rs.getInt("rating")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des feedbacks : " + e.getMessage());
        }
        return feedbacks;
    }

    // Supprimer un feedback
    public boolean deleteFeedback(int id) {
        String query = "DELETE FROM feedback WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Feedback supprimé !");
                return true;
            } else {
                System.out.println("⚠️ Aucun feedback trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression du feedback : " + e.getMessage());
        }
        return false;
    }

    // Mettre à jour un feedback
    public boolean updateFeedback(Feedback feedback) {
        String query = "UPDATE feedback SET client_id = ?, event_id = ?, comments = ?, rating = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, feedback.getClientId());
            if (feedback.getEventId() != null) {
                pstmt.setInt(2, feedback.getEventId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, feedback.getComments());
            pstmt.setInt(4, feedback.getRating());
            pstmt.setInt(5, feedback.getId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Feedback mis à jour !");
                return true;
            } else {
                System.out.println("⚠️ Feedback non trouvé !");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
        return false;
    }



}
