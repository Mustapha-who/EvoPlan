package evoplan.controllers.feedback;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.List;

public class RatingDialog extends Dialog<Integer> {
    private final List<String> questions = List.of(
            "Was the service provided satisfactory?",
            "Did the event meet your expectations?",
            "Was the staff helpful and professional?",
            "Would you recommend this service to others?",
            "Was the overall experience positive?"
    );

    public RatingDialog() {
        setTitle("Rate Your Experience");
        setHeaderText("Please answer the following questions about your experience:");

        // Create the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 20, 10, 20));

        List<ToggleGroup> toggleGroups = new ArrayList<>();

        // Add questions with Yes/No radio buttons
        for (String question : questions) {
            ToggleGroup group = new ToggleGroup();
            RadioButton yesButton = new RadioButton("Yes");
            RadioButton noButton = new RadioButton("No");
            yesButton.setToggleGroup(group);
            noButton.setToggleGroup(group);

            VBox questionBox = new VBox(5);
            questionBox.getChildren().addAll(
                    new Label(question),
                    new ButtonBar() {{
                        getButtons().addAll(yesButton, noButton);
                    }}
            );
            content.getChildren().add(questionBox);
            toggleGroups.add(group);
        }

        // Add dialog buttons
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);

        // Set the content
        getDialogPane().setContent(content);

        // Convert the result
        setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                // Calculate rating based on number of "Yes" answers
                long yesCount = toggleGroups.stream()
                        .filter(group -> group.getSelectedToggle() != null)
                        .filter(group -> ((RadioButton) group.getSelectedToggle()).getText().equals("Yes"))
                        .count();

                // Convert yes count to rating (1-5)
                return (int) yesCount;
            }
            return null;
        });
    }
}