package su.logix.studperformance.main.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import su.logix.studperformance.main.Core.Controller;
import su.logix.studperformance.main.Models.KeyModel;
import su.logix.studperformance.main.Models.OptionsModel;

import java.sql.SQLException;
import java.util.Optional;

public class AdminController extends Controller {

    private static final Logger log = Logger.getLogger(AdminController.class);

    @FXML
    public void setHeadPass() {
        PasswordDialog dialog = new PasswordDialog("Установка ключа заведующего", "Введие новый ключ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                KeyModel.setNewHeadKey(getHash(result.get()));
            } catch (SQLException e) {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить новый ключ: " + e.getMessage());
                log.error("save head key error", e);
            }
        }
    }

    @FXML
    public void setAdminPass() {
        PasswordDialog dialog = new PasswordDialog("Установка пароля администратора", "Введие новый пароль");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                KeyModel.setNewAdminKey(getHash(result.get()));
            } catch (SQLException e) {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить новый пароль: " + e.getMessage());
                log.error("save admin pass error", e);
            }
        }
    }

    @FXML
    public void setUserLogin() {
        Optional<String> result = showTextInputDialog("Установка логина пользователя", "Введите новый логин");
        if (result.isPresent()) {
            try {
                KeyModel.setNewUserLogin(result.get());
            } catch (SQLException e) {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить новый логин: " + e.getMessage());
                log.error("save user login error", e);
            }
        }
    }

    @FXML
    public void setUserPass() {
        PasswordDialog dialog = new PasswordDialog("Установка пароля пользователя", "Введие новый пароль");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                KeyModel.setNewUserPass(getHash(result.get()));
            } catch (SQLException e) {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить новый пароль: " + e.getMessage());
                log.error("save user pass error", e);
            }
        }
    }

    private Optional<String> showTextInputDialog(String title, String headerText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setGraphic(null);
        GridPane gridPane = (GridPane) dialog.getEditor().getParent();
        gridPane.setPadding(new Insets(30, 40, 20, 30));
        dialog.getEditor().setPrefWidth(200);
        return dialog.showAndWait();
    }

    private void setDate(int date) {
        Optional<String> result = showTextInputDialog("Установка сроков " + date + " аттестации", "Введите информацию");
        if (result.isPresent()) {
            try {
                OptionsModel.setDate(date, result.get());
            } catch (SQLException e) {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить срок аттестации: " + e.getMessage());
                e.printStackTrace();
                log.error("save dates error", e);
            }
        }
    }

    @FXML
    public void setDate1() {
        setDate(1);
    }

    @FXML
    public void setDate2() {
        setDate(2);
    }

    @FXML
    public void setDate3() {
        setDate(3);
    }

    private class PasswordDialog extends Dialog<String> {
        private PasswordField passwordField1;
        private PasswordField passwordField2;

        PasswordDialog(String title, String headerText) {
            setTitle(title);
            setHeaderText(headerText);
            ButtonType passwordButtonType = new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);
            passwordField1 = new PasswordField();
            passwordField1.setPromptText("Пароль");
            passwordField1.setPrefWidth(200);
            passwordField2 = new PasswordField();
            passwordField2.setPromptText("Повторите пароль");
            passwordField2.setPrefWidth(200);
            VBox vBox = new VBox();
            vBox.getChildren().addAll(passwordField1, passwordField2);
            vBox.setPadding(new Insets(20, 50, 20, 50));
            vBox.setSpacing(20);
            getDialogPane().setContent(vBox);
            Platform.runLater(() -> passwordField1.requestFocus());
            setResultConverter(dialogButton -> {
                if (dialogButton == passwordButtonType) {
                    if (passwordField1.getText().equals(passwordField2.getText())) {
                        return passwordField1.getText();
                    } else {
                        showMessage(Alert.AlertType.ERROR, "Ошибка", "Пароли не совпадают");
                    }
                }
                return null;
            });
        }

        public PasswordField getPasswordField1() {
            return passwordField1;
        }

        public PasswordField getPasswordField2() {
            return passwordField2;
        }
    }
}
