package su.logix.studperformance.main.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;
import su.logix.studperformance.main.Core.Controller;
import su.logix.studperformance.main.Models.OptionsModel;

import java.sql.SQLException;

public class OptionsController extends Controller {

    private static final Logger log = Logger.getLogger(OptionsController.class);

    @FXML
    public TextField tfDepartment;
    @FXML
    public TextField tfResponsibleName;
    @FXML
    public TextField tfHeadName;

    private OptionsModel optionsModel;

    @FXML
    public void initialize() {
        optionsModel = new OptionsModel();
        try {
            optionsModel.load();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("options model load error", e);
        }
        tfDepartment.setText(optionsModel.getDepartment());
        tfResponsibleName.setText(optionsModel.getResponsibleName());
        tfHeadName.setText(optionsModel.getHeadName());
    }

    @FXML
    public void save() {
        optionsModel.setDepartment(tfDepartment.getText());
        optionsModel.setResponsibleName(tfResponsibleName.getText());
        optionsModel.setHeadName(tfHeadName.getText());
        try {
            optionsModel.save();
            showMessage(Alert.AlertType.INFORMATION, "Успешно", "Настройки сохранены");
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить настройки: " + e.getMessage());
            e.printStackTrace();
            log.error("options model save error", e);
        }
    }
}
