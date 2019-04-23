package su.logix.studperformance.main.Core;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.codec.digest.DigestUtils;

public class Controller {

    protected String getHash(String input) {
        return DigestUtils.sha1Hex(DigestUtils.md5Hex(input));
    }

    protected void showMessage(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }
}
