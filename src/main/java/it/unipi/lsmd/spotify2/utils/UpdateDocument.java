package it.unipi.lsmd.spotify2.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateDocument {

    public static void setLabelClickHandler(Label label, TextField textField) {
        updateDocument(label, textField);
    }

    public static void setLabelClickHandler(Label label, ComboBox<String> comboBox) {
        updateDocument(label, comboBox);
    }
    public static void updateDocument(Label label, TextField textField) {
        label.setOnMouseClicked(event -> {
            label.setVisible(false);
            textField.setVisible(true);
            textField.setEditable(true);
            textField.setManaged(true);
            textField.setText(label.getText());
            textField.requestFocus(); // Focus on the text field
        });

        textField.setOnAction(event -> {
            label.setText(textField.getText());
            label.setVisible(true);
            textField.setVisible(false);
            textField.setManaged(false);
        });
    }

    public static void updateDocument(Label label, ComboBox<String> comboBox) {
        label.setOnMouseClicked(event -> {
            label.setVisible(false);
            comboBox.setVisible(true);
            comboBox.setEditable(true);
            comboBox.setManaged(true);
            comboBox.requestFocus(); // Focus on the text field
        });

        comboBox.setOnAction(event -> {
            label.setText(comboBox.getValue());
            label.setVisible(true);
            comboBox.setVisible(false);
            comboBox.setManaged(false);
        });
    }
}
