package it.unipi.lsmd.spotify2.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertMessages {
    public static void informationAlert(String message) {
        // Create a new alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Show the alert
        alert.showAndWait();
    }

    public static boolean confirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Customize the OK and Cancel buttons
        ButtonType okButton = new ButtonType("OK");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(okButton, cancelButton);

        // Show the dialog and wait for user input
        Optional<ButtonType> result = alert.showAndWait();

        // Check the user's choice
        return result.isPresent() && result.get() == okButton;
    }
}
