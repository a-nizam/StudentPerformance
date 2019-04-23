package su.logix.studperformance.main.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import su.logix.studperformance.main.Core.Controller;
import su.logix.studperformance.main.Models.KeyModel;
import su.logix.studperformance.main.Models.OptionsModel;
import su.logix.studperformance.main.connection.DBConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class MainController extends Controller {

    private static final Logger log = Logger.getLogger(MainController.class);

    private static boolean loggedIn = false;
    @FXML
    public Button btnSheet;
    @FXML
    public Button btnSubjectLoad;
    @FXML
    public Button btnStudentsLoad;
    @FXML
    public Button btnExport;
    @FXML
    public MenuItem miSettings;
    @FXML
    public TextField tfLogin;
    @FXML
    public PasswordField pfPass;
    @FXML
    public Button btnLogin;
    @FXML
    public Label lblCert1;
    @FXML
    public Label lblCert2;
    @FXML
    public Label lblCert3;

    @Contract(pure = true)
    public static boolean isLoggedIn() {
        return loggedIn;
    }

    private static void setLoggedIn(boolean loggedIn) {
        MainController.loggedIn = loggedIn;
    }

    @FXML
    public void initialize() {
        String url = "jdbc:sqlite:data.db3";
        DBConnection dbConnection = DBConnection.getInstance();
        dbConnection.setParams(url);
        disableButtons(true);
        try {
            dbConnection.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось подключиться к базе данных: " + e.getMessage());
            log.error("db connection error", e);
        }
        initDates();
    }

    private void initDates() {
        try {
            Map<Integer, String> dates = OptionsModel.getDates();
            lblCert1.setText(dates.get(1));
            lblCert2.setText(dates.get(2));
            lblCert3.setText(dates.get(3));
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить сроки проведения аттестаций");
            e.printStackTrace();
            log.error("dates loading error", e);
        }
    }

    private void disableButtons(boolean disable) {
        btnSheet.setDisable(disable);
        btnSubjectLoad.setDisable(disable);
        btnStudentsLoad.setDisable(disable);
        btnExport.setDisable(disable);
        miSettings.setDisable(disable);
    }

    @FXML
    public void fillSheet() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/sheet.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Аттестационный лист");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loading sheet form error", e);
        }
    }

    @FXML
    public void uploadUP() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/subjectsUpload.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Загрузка предметов");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loading subject uploading form error", e);
        }
    }

    @FXML
    public void uploadStudents() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/studentsUpload.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Загрузка студентов");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loading students uploading form error", e);
        }
    }

    @FXML
    public void exportCertification() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/certificationExport.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Выгрузка аттестационных листов");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loading certification export form error", e);
        }
    }

    @FXML
    public void showOptionsForm() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/options.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Настройки");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loading options form error", e);
        }
    }

    @FXML
    public void close() {
        Platform.exit();
    }

    @FXML
    public void showHelpForm() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/help.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Помощь");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loading help form error", e);
        }
    }

    @FXML
    public void login() {
        if (tfLogin.getText().isEmpty() || pfPass.getText().isEmpty()) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не введены логин и/или пароль");
        } else {
            KeyModel keyModel = new KeyModel();
            try {
                keyModel.loadAuthKeys();
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("auth data loading error", e);
            }
            if (tfLogin.getText().equals(keyModel.getUserLogin()) && getHash(pfPass.getText()).equals(keyModel.getUserPass())) {
                MainController.setLoggedIn(true);
                disableButtons(false);
                disableAuthWindow();
            } else {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Неверные логин и/или пароль");
            }
        }
    }

    private void disableAuthWindow() {
        tfLogin.setDisable(true);
        pfPass.setDisable(true);
        btnLogin.setDisable(true);
    }

    @FXML
    public void showAdminForm() {
        PasswordDialog dialog = new PasswordDialog();
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            KeyModel keyModel = new KeyModel();
            try {
                keyModel.loadAdminKey();
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("loading admin key error", e);
            }
            if (getHash(result.get()).equals(keyModel.getAdminKey())) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
                    Stage stage = new Stage();
                    stage.setTitle("Панель администрирования");
                    stage.setScene(new Scene(root));
                    stage.show();
                    stage.setOnCloseRequest(event -> initDates());
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("loading admin form error", e);
                }
            } else {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Неверный ключ администратора");
            }
        }
    }

    private class PasswordDialog extends Dialog<String> {
        private PasswordField passwordField;

        PasswordDialog() {
            setTitle("Авторизация");
            setHeaderText("Введите ключ администратора");
            ButtonType passwordButtonType = new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);
            passwordField = new PasswordField();
            passwordField.setPromptText("Пароль");
            HBox hBox = new HBox();
            hBox.getChildren().add(passwordField);
            hBox.setPadding(new Insets(20));
            HBox.setHgrow(passwordField, Priority.ALWAYS);
            getDialogPane().setContent(hBox);
            Platform.runLater(() -> passwordField.requestFocus());
            setResultConverter(dialogButton -> {
                if (dialogButton == passwordButtonType) {
                    return passwordField.getText();
                }
                return null;
            });
        }

        public PasswordField getPasswordField() {
            return passwordField;
        }
    }
}