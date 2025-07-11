package evoplan.main;
/*import evoplan.entities.*;
import evoplan.entities.user.Administrator;
import evoplan.entities.user.Client;
import evoplan.entities.user.EventPlanner;
import evoplan.entities.user.Instructor;
import evoplan.entities.workshop.workshop;
import evoplan.services.PartnerService;
import evoplan.services.user.*;
import evoplan.services.workshop.workshopService;
import evoplan.entities.feedback.Claim;
import evoplan.entities.feedback.Feedback;
import evoplan.services.feedback.ClaimService;
import evoplan.services.feedback.FeedbackService;/*/
import evoplan.entities.ressource.Equipment;
import evoplan.entities.ressource.Venue;
import evoplan.services.ressource.RessourceService;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import static evoplan.entities.user.EventPlannerModule.LOGISTICS;

public class Main {

    public static void main(String[] args) {

        DatabaseConnection.getInstance();
        /*package evoplan.main;/*/
        // ✅ Création de l'instance du service
        RessourceService ressourceService = new RessourceService();

        System.out.println("\n=== 🔹 TEST : AJOUT DE RESSOURCES ===");
        Equipment equip1 = new Equipment("Ordinateur", "Matériel", true, "Informatique", 10);
        Venue salle1 = new Venue("Salle de conférence", "Salle", true, "123 Rue Centrale", 50); // Utilisez le constructeur avec 5 arguments
        ressourceService.addRessource(equip1);
        ressourceService.addRessource(salle1);

        System.out.println("\n=== 🔹 TEST : LECTURE DES RESSOURCES ===");
        ressourceService.getAllRessources().forEach(System.out::println);
        System.out.println("\n=== 🔹 TEST : MISE À JOUR ===");
        equip1.setQuantity(15);
        ressourceService.updateRessource(equip1);
        System.out.println("✅ Mise à jour réussie !");

        System.out.println("\n=== 🔹 TEST : SUPPRESSION ===");
        ressourceService.deleteRessource(equip1.getId());
        ressourceService.deleteRessource(salle1.getId());
        System.out.println("✅ Suppression réussie !");
    }
}





/*

        EvenementService es=new EvenementService();
        evenement e = new evenement("asbegi","ben","djsfdjodkf","20-5-2015","1-2-2023",100,20,"sdfjk",1);
        es.addEvent(e);
        es.afficherEvents();
        int idsupp=11;
        evenement eventToDelete = new evenement(); // Create an empty Partner object
        eventToDelete.setIdEvent(idsupp);  // Set the ID of the Partner to delete
        es.deleteEvent(eventToDelete);
        System.out.println("Partner with ID " + idsupp + " deleted successfully.");


        // Afficher la liste après suppression
        System.out.println("\nListe des événements après suppression :");
        es.afficherEvents();
        int idAModifier = 12;

        // 🔹 Créer un objet événement mis à jour
        evenement eventModifie = new evenement(idAModifier, "Conférence IA", "Conférence sur l'intelligence artificielle", "2025-04-10", "2025-04-12", "Lyon", 300, 80.0, "Confirmé", 2);

        // 🔹 Modifier l'événement
        System.out.println("\nModification de l'événement avec ID : " + idAModifier);
        es.updateEvent(eventModifie);

        // 🔹 Afficher la liste après modification
        System.out.println("\nListe des événements après modification :");
        es.afficherEvents();
*/
//PartnerService ps= new PartnerService();
        /*Partner p = new Partner("sponsor","ademayechi@esprit.tn","25268488","C:/Users/LENOVO/Pictures/Screenshots/Capture d'écran 2024-10-09 094357");
        ps.ajouter(p);*/
//ps.getall();
       /* int idmodifier =2;
        Partner m = new Partner( idmodifier,"speaker","ademayechi@esprit.tn","25268488","C:/Users/LENOVO/Pictures/Screenshots/Capture d'écran 2024-10-09 094357");
        ps.modifier(m);
        System.out.println("✅ Partner updated successfully!");
        */
        /*int idsupp=2;
        Partner partnerToDelete = new Partner(); // Create an
        partnerToDelete.setId_partner(idsupp);  // Set the ID of empty Partner objectthe Partner to delete
        ps.supprimer(partnerToDelete);
        System.out.println("Partner with ID " + idsupp + " deleted successfully.");

        ps.getall();*/


// Workshop Section //

// Create a WorkshopService instance //


//   Add workshop //
                /*
                workshop workshop = new workshop( 0, // I set the id to 0 because it auto increment in phpmyadmin
                        "Health and Wellness Workshop", // title
                        Date.valueOf("2025-05-03"), // date
                        1, // instructorId (assuming instructor ID 1 exists)
                        1, // sessionId (assuming session ID 1 exists)
                        30, // capacity
                        120, // duration in minutes
                        "Room 101", // location
                        "A workshop focused on health and wellness." // description
                );

                workshopService.addWorkshop(workshop);
                System.out.println("Workshop added successfully!");
            */


// Delete the workshop by id //
                /*
                workshopService.deleteWorkshop(14); // I chose id 14 for example
                System.out.println("Workshop deleted successfully!");
                */


// Update the workshop //
            /*
                workshop workshopToUpdate = new workshop( 0,
                        "new updated", // title
                        Date.valueOf("2021-04-03"), // date
                        1, // instructorId
                        1, // sessionId
                        30, // capacity
                        120, // duration
                        "Room 101", // location
                        "A workshop focused on health and wellness, now with updated details." // description
                );

                workshopService.updateWorkshop(workshopToUpdate);

                System.out.println("Workshop updated successfully!");
                */

// Display workshop data//




/*

                // PartnershipService ps = new PartnershipService();

                // Adding a new partnership
                /*Partnership newPartnership = new Partnership(1, 1, "2025-02-14", "2025-10-31", "partner_logo.jpeg");
                ps.ajouter(newPartnership);*/

// Getting all partnerships
               /* System.out.println("📋 List of all partnerships:");
                ps.getAll().forEach(System.out::println);


                /*int idToUpdate = 3;
                Partnership updatedPartnership = new Partnership(idToUpdate, 1, 1, "2025-03-01", "2025-11-30", "updated_logo.hiii");
                ps.modifier(updatedPartnership);*/
                /*int idToDelete = 4; // The ID of the partnership to delete

 /**/


//
// }


//}













