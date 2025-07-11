package evoplan.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {


    public void start(Stage primaryStage) throws Exception {

        DatabaseConnection.getInstance();



        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        //Parent root = FXMLLoader.load(getClass().getResource("/ClientHomePage/WorkshopFrontEnd/ReserveWorkshop.fxml"));

        //Parent root = FXMLLoader.load(getClass().getResource("/ClientHomePage/WorkshopFrontEnd/Workshops.fxml"));
        //Parent root = FXMLLoader.load(getClass().getResource("/ClientHomePage/Events/DisplayEventsClient.fxml"));

        //Parent root = FXMLLoader.load(getClass().getResource("/ressource/AjouterRessource.fxml"));

        //Parent root = FXMLLoader.load(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));



        //Parent root = FXMLLoader.load(getClass().getResource("/ressource/AjouterRessource.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();




        // ===== Partie de tes amis (commentée pour tester seulement ta partie) =====
        /*
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/evoplan/views/crudfeedback.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setTitle("Gestion des Feedbacks");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.out.println("❌ Erreur de chargement de l'interface des feedbacks : " + e.getMessage());
        }
        */
    }
    //EventPlannerHomePage/EventPLannerHomePage

    public static void main(String[] args) {
        launch(args);
    }
}
