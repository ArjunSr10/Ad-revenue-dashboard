package main.dash.scene.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import main.dash.enums.SceneName;
import main.dash.event.SceneListener;
import main.dash.scene.DataView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ManualController {

    @FXML
    private Parent root;
    @FXML
    private Button returnButton;
    @FXML
    private ImageView pdfImageView;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;

    private DataView view;
    private PDDocument document;
    private PDFRenderer pdfRenderer;
    private int currentPage = 0;
    private SceneName returnScene;              // (a)
    public void setReturnScene(SceneName s) {   // (b)
        this.returnScene = s;
    }

    public void handleEvents(SceneListener sceneListener) {
        returnButton.setOnAction(e -> {
            if (returnScene != null) {
                sceneListener.sceneChanged(returnScene);
            } else {
                // fallback if someone forgot to set it
                sceneListener.sceneChanged(SceneName.DASHBOARD);
            }
        });
    }

    public void setView(DataView view) {
        this.view = view;
    }

    public void initialize() {
        String pdfFilePath = "src/main/resources/helpdocument.pdf";
        renderPDF(pdfFilePath);
        previousButton.setOnAction(e -> showPreviousPage());
        nextButton.setOnAction(e -> showNextPage());
    }

    /**
     * render pdf
     * @param filePath the path of pdf
     */
    private void renderPDF(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                System.err.println("File not found: " + filePath);
                return;
            }
            document = PDDocument.load(pdfFile);
            pdfRenderer = new PDFRenderer(document);
            renderPage(currentPage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * redenr page
     * @param pageIndex the index of page
     */
    private void renderPage(int pageIndex) {
        try {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 300);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            pdfImageView.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderPage(currentPage);
        }
    }
    private void showNextPage() {
        if (currentPage < document.getNumberOfPages() - 1) {
            currentPage++;
            renderPage(currentPage);
        }
    }
}
