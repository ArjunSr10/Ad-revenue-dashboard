package main.dash.common;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AlarmUtils {
    /**
     * An alert box pops up
     * @param content the content of alert
     */
    public static void alarm(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tips");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * An confirmation box pops up
     * @param title the title of box
     * @param content the content of box
     * @param onOk the callback when click ok
     * @param onCancel the callback when click cancel
     */
    public static void confirmation(String title, String content, Supplier onOk, Supplier onCancel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(buttonType -> {
            if(buttonType== ButtonType.OK){
                onOk.get();
            }else{
                onCancel.get();
            }
        });
    }
}
