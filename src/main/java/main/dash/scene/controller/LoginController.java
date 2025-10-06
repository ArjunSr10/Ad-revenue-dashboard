package main.dash.scene.controller;

import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import main.dash.common.AlarmUtils;
import main.dash.common.Context;
import main.dash.common.OperationLogger;
import main.dash.database.user.usersDatabase;
import main.dash.enums.SceneName;
import main.dash.event.SceneListener;
import main.dash.scene.DataView;

public class LoginController {
	@FXML
	private Button loginButton, createText, manualButton;

	@FXML
	private TextField usernameText, passwordText;

	@FXML
	private ImageView logo;

	@FXML
	private TilePane pane;

	@FXML
	private Text loginText;

	private DataView view;

	@FXML
	private void initialize(){
		logo.setImage(new Image(Objects.requireNonNull(getClass().getResource("/UoS logo.png")).toExternalForm()));
	}

	/**
	 * set callback of create user ,login,help
	 * @param sceneListener the callback of click
	 */
	public void handleEvents(SceneListener sceneListener){
		createText.setOnAction(e -> {
			if (createAccount(usernameText.getText(), passwordText.getText())) {
				Context.setCurrentUser(usernameText.getText());
				OperationLogger.recordCreateUser("Successful account creation. Username: " + usernameText.getText());
				sceneListener.sceneChanged(SceneName.DASHBOARD);
			}else{
				AlarmUtils.alarm("Account creation failed. Ensure a username and password has been entered.");
			}
		});

		loginButton.setOnAction(e -> {
			if (authenticateUser(usernameText.getText(), passwordText.getText())) {
				Context.setCurrentUser(usernameText.getText());
				OperationLogger.recordLogin("Successful login attempt. Username: " + usernameText.getText());
				sceneListener.sceneChanged(SceneName.DASHBOARD);
				view.restrictActions();
			}else{
				OperationLogger.recordLogin("Failed login attempt. Username: "+usernameText.getText());
				AlarmUtils.alarm("The user does not exist or the password is incorrect.");
			}
		});

		manualButton.setOnAction(e -> {
			OperationLogger.recordAction("Click Help->Manual");
			sceneListener.sceneChanged(SceneName.MANUAL);
			view.getManualController().setReturnScene(SceneName.LOGIN);
		});
	}

	/**
	 * create a user into database
	 * @param username the username of new user
	 * @param password the password of new password
	 * @return the result of create user,true is success, false is fail
	 */
	public boolean createAccount(String username, String password) {
		if (username==null||username.isEmpty()||password==null||password.isEmpty()){
			return false;
		}
		// If the account created is the first one in the database, they receive admin privileges by default
		if (usersDatabase.getNumberOfUsers() == 0) {
			usersDatabase.insertUser(username, password, "Admin");
		} else {
			usersDatabase.insertUser(username, password, "Viewer");
		}
		return true;
	}

	/**
	 * login
	 * @param username the username of  user
	 * @param password the password of  password
	 * @return
	 */
	public boolean authenticateUser(String username, String password) {
		if (username==null||username.isEmpty()||password==null||password.isEmpty()){
			return false;
		}
		return usersDatabase.authenticateUser(username, password);
	}

	private void setFontSize(){
		view.bindFont(pane, 1);
		view.bindFont(createText, 1.2f);
		view.bindFont(loginButton, 1.2f);
		view.bindFont(loginText, 1.5f);
	}

	public void setView(DataView view) {
		this.view = view;
		setFontSize();
	}
}
