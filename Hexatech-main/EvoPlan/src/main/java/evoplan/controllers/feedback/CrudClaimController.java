package evoplan.controllers.feedback;

import evoplan.entities.feedback.Claim;
import evoplan.services.feedback.ClaimService;  // Correction du nom du service pour correspondre à ClaimService
import java.sql.Date;

public class CrudClaimController {
    private ClaimService claimService = new ClaimService(); // Utilisation de ClaimService et non FeedbackService

    // Méthode pour gérer l'ajout d'une réclamation
    public boolean addClaim(String type, String description, String creationDateStr, String status, int issuedBy) {
        try {
            // Conversion de la chaîne de date en java.sql.Date
            Date creationDate = Date.valueOf(creationDateStr);

            // Conversion des String 'type' et 'status' en Enums
            Claim.ClaimType claimType = Claim.ClaimType.valueOf(type.toUpperCase());
            Claim.ClaimStatus claimStatus = Claim.ClaimStatus.valueOf(status.toUpperCase());

            // Création de l'objet Claim avec les paramètres appropriés
            Claim claim = new Claim(
                    "0", // ID fictif ou généré par la base de données
                    description,
                    claimType,
                    creationDate,
                    claimStatus,
                    issuedBy,
                    null // eventId is null for app-related claims
            );

            // Appel du service pour ajouter la réclamation dans la base de données
            claimService.addClaim(claim);

            return true; // Retourner vrai après l'ajout
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de format : " + e.getMessage());
        }
        return false;
    }
}
