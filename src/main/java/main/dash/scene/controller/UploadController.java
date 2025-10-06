package main.dash.scene.controller;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.dash.data.DataController;
import main.dash.data.DataModel;
import main.dash.enums.FileType;
import main.dash.enums.Metric;
import main.dash.enums.SceneName;
import main.dash.event.FileListener;
import main.dash.event.SceneListener;
import main.dash.scene.DataView;

public class UploadController {
	@FXML
	private Button returnButton;

	@FXML
	private Node root;

	@FXML
	private Text uploadText;

	@FXML
	private ImageView loadingIcon;

	private DataView view;

	private FileListener fileListener;

	public void handleEvents(SceneListener sceneListener){
		returnButton.setOnAction(e -> sceneListener.sceneChanged(SceneName.DASHBOARD));
	}

	/**
	 * the action to click upload
	 * @param actionEvent
	 */
	@FXML
	private void upload(javafx.event.ActionEvent actionEvent) {
		Button itemToUpload = (Button) actionEvent.getSource();
		String item = itemToUpload.getUserData().toString();
		FileType fileType = Enum.valueOf(FileType.class, item);

		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("src"));

		File selectedFile = fileChooser.showOpenDialog(view.getStage());

		if(selectedFile == null) return;
		fileListener.fileChanged(selectedFile.getAbsolutePath(), fileType);
	}

	public void setFileListener(FileListener fileListener) {
		this.fileListener = fileListener;
	}

	private void setFontSize(){
		view.bindFont(root, 1);
		view.bindFont(uploadText, 2f);
	}

	public void showLoading(boolean show){
		loadingIcon.setVisible(show);
	}


	/**
	 * the resource settings of data view
	 * @param view
	 */
	public void setView(DataView view) {
		this.view = view;
		setFontSize();

		loadingIcon.setImage(new Image(
						Objects.requireNonNull(getClass().getResource("/loading.gif")).toExternalForm()));
	}
}
