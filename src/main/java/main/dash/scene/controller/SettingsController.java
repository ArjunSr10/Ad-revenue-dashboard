package main.dash.scene.controller;

import java.util.Objects;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import main.dash.common.OperationLogger;
import main.dash.enums.ColourScheme;
import main.dash.enums.SceneName;
import main.dash.event.SceneListener;
import javafx.scene.control.Button;
import main.dash.scene.DataView;

public class SettingsController {
    @FXML
    private Parent root;

    @FXML
    private Text settingsText;

    @FXML
    private Button returnButton;

    @FXML
    private Slider textSize;

    @FXML
    private ComboBox<String> colourScheme;

    private DataView view;

    private void setFontSize() {
        view.bindFont(root, 1);
        view.bindFont(settingsText, 2f);

        textSize.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            OperationLogger.recordAction("update text size,new size:" + textSize.getValue());
        });
    }

    public Slider getFontSize() {
        return textSize;
    }

    public ColourScheme getColourScheme() {
        return ColourScheme.valueOf(Objects.requireNonNull(colourScheme.getValue()).toUpperCase());
    }

    public void handleEvents(SceneListener sceneListener) {
        returnButton.setOnAction(e -> sceneListener.sceneChanged(SceneName.DASHBOARD));
    }

    public void setView(DataView view) {
        this.view = view;
        setFontSize();

        colourScheme.setOnAction(e -> {
            view.setColourScheme(getColourScheme());

            OperationLogger.recordAction("update colour scheme,new scheme:" + colourScheme.getValue());
        });
    }
}
