package main.dash.scene.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class BounceFields {

	@FXML
	private TextField bounceInput, pagesInput;

	@FXML
	private void initialize() {
		initializeField(bounceInput);
		initializeField(pagesInput);
	}

	private void initializeField(TextField field){
		field.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				field.setText(newValue.replaceAll("\\D", ""));
			}
		});
	}

	public void handleUpdates(ChangeListener<String> listener){
		bounceInput.textProperty().addListener(listener);
		pagesInput.textProperty().addListener(listener);
	}

	public int getBounceTime() {
		try{
			return Integer.parseInt(bounceInput.getText());
		} catch (NumberFormatException e){
			return 0;
		}
	}

	public int getPagesViewed() {
		try{
			return Integer.parseInt(pagesInput.getText());
		} catch (NumberFormatException e){
			return 0;
		}
	}
}
