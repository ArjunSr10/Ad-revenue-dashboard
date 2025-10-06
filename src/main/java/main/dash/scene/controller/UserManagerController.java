package main.dash.scene.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import main.dash.common.AlarmUtils;
import main.dash.common.Context;
import main.dash.common.OperationLogger;
import main.dash.database.user.usersDatabase;
import main.dash.enums.SceneName;
import main.dash.event.SceneListener;
import main.dash.scene.DataView;
import java.util.function.Supplier;


public class UserManagerController {
    @FXML
    private TilePane tilePane;

    @FXML
    private Text userManagerText;

    @FXML
    private TextField usernameText;

    @FXML
    private MenuItem adminButton, editorButton, viewerButton;

    @FXML
    private Button selectUserButton, removeUserButton, returnButton;

    @FXML
    private MenuButton chooseRoleButton;
    @FXML
    private Text userRoleText;


    private DataView view;



    private void setFontSize(){
        view.bindFont(tilePane, 1);
        view.bindFont(userManagerText, 2f);
    }

    /**
     * set callback of select user,remove user and other
     * @param sceneListener
     */
    public void handleEvents(SceneListener sceneListener) {
        selectUserButton.setOnAction(e -> {
            // Check the user is in the database and is a valid user
            if (usernameText.getText().equals(Context.getCurrentUser())) {
                AlarmUtils.alarm("Cannot remove yourself from the database / change your own role!");
            } else {
                String role = usersDatabase.getRole(usernameText.getText());
                if (role != null) {
                    removeUserButton.setVisible(true);
                    chooseRoleButton.setVisible(true);
                    userRoleText.setText("Current Role: " + role); // <-- show the user's role
                } else {
                    AlarmUtils.alarm("User not found!");
                    userRoleText.setText(""); // Clear if not found
                }
            }
        });

        removeUserButton.setOnAction(e -> {
            String user = usernameText.getText();
            AlarmUtils.confirmation(
                    "Confirm User Removal",
                    "Are you sure you want to remove “" + user + "?”",
                    // onOk: perform removal + logging
                    (Supplier<Void>) () -> {
                        usersDatabase.removeUser(user);
                        OperationLogger.recordAction(
                                "User " + user + " removed from users database by "
                                        + Context.getCurrentUser()
                        );
                        usernameText.clear();
                        userRoleText.setText("");
                        removeUserButton.setVisible(false);
                        chooseRoleButton.setVisible(false);
                        AlarmUtils.alarm("User '" + user + "' was successfully removed.");
                        return null;
                    },
                    // onCancel: do nothing
                    (Supplier<Void>) () -> null
            );
        });


        // Wrap each role change in a confirmation dialog:
        adminButton.setOnAction(e -> confirmAndChangeRole("Admin"));
        editorButton.setOnAction(e -> confirmAndChangeRole("Editor"));
        viewerButton.setOnAction(e -> confirmAndChangeRole("Viewer"));

        returnButton.setOnAction(e -> {
            usernameText.clear();
            removeUserButton.setVisible(false);
            chooseRoleButton.setVisible(false);
            userRoleText.setText(""); // <-- clear on return
            sceneListener.sceneChanged(SceneName.DASHBOARD);
        });
    }

    private void confirmAndChangeRole(String newRole) {
        String user = usernameText.getText();
        AlarmUtils.confirmation(
                "Confirm Role Change",
                "Are you sure you want to change '" + user + "' to role: " + newRole + "?",
                // on OK:
                (Supplier<Void>) () -> {
                    usersDatabase.changeUserRole(user, newRole);
                    OperationLogger.recordAction(
                            "User " + user + "'s role has been changed to " + newRole + " by " + Context.getCurrentUser()
                    );
                    userRoleText.setText("Current Role: " + newRole);
                    return null;
                },
                // on Cancel:
                (Supplier<Void>) () -> null
        );
    }

    public void setView(DataView view) {
        this.view = view;
        setFontSize();
    }
}

