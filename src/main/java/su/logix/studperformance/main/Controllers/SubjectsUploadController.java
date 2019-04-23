package su.logix.studperformance.main.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.logix.studperformance.main.Core.UploadController;
import su.logix.studperformance.main.Models.SubjectModel;
import su.logix.studperformance.main.connection.DBConnection;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


public class SubjectsUploadController extends UploadController {

    private static final Logger log = Logger.getLogger(SubjectsUploadController.class);

    @FXML
    public TextField tfFile;

    @FXML
    public void initialize() {

    }

    @FXML
    public void showXMLFileChooser() {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        super.showFileDialog(tfFile, extFilter);
    }

    @FXML
    public void load() {
        try {
            if (tfFile.getText() != null && !tfFile.getText().trim().isEmpty()) {
                Set<SubjectModel> subjectSet = new HashSet<>();
                loadSubjectsFromXML(tfFile.getText(), subjectSet);
                saveSubjects(subjectSet);
            }
        } catch (XMLStreamException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Возникла ошибка при чтении xml файла");
            e.printStackTrace();
            log.error("xml stream reader error", e);
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Ошибка ввода/вывода");
            e.printStackTrace();
            log.error("close input stream error", e);
        }
    }


    private void saveSubjects(@NotNull Set<SubjectModel> subjectSet) {
        DBConnection connection = DBConnection.getInstance();
        boolean oldAutoCommitValue = false;
        try {
            if (oldAutoCommitValue = connection.getAutoCommitValue()) {
                connection.disableAutoCommit();
            }
            for (SubjectModel subjectModel : subjectSet) {
                subjectModel.save();
            }
            connection.commit();
            showMessage(Alert.AlertType.INFORMATION, "Выполнено", "Дисциплины загружены");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                log.error("rollback error", e);
            }
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить список предметов в базу данных");
            e.printStackTrace();
            log.error("save subjects error", e);
        } finally {
            if (oldAutoCommitValue) {
                try {
                    connection.enableAutoCommit();
                } catch (SQLException e) {
                    e.printStackTrace();
                    log.error("enable autocommit error", e);
                }
            }
        }
    }

    /**
     * @param filePath   Путь до файла
     * @param subjectSet Пустой список предметов для заполнения
     */
    private void loadSubjectsFromXML(String filePath, Set<SubjectModel> subjectSet) throws XMLStreamException, IOException {
        XMLStreamReader xmlStreamReader = getXmlStreamReader(filePath);
        int event;
        ActionType action = ActionType.SKIP;
        String localName;
        SubjectModel subjectModel = new SubjectModel();
        while (xmlStreamReader.hasNext()) {
            event = xmlStreamReader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    localName = xmlStreamReader.getLocalName();
                    switch (localName) {
                        case "Subject":
                            subjectModel = new SubjectModel();
                            action = ActionType.SKIP;
                            break;
                        case "Faculty":
                            action = ActionType.READ_FACULTY;
                            break;
                        case "SubjectName":
                            action = ActionType.READ_SUBJECT;
                            break;
                        case "Semester":
                            action = ActionType.READ_SEMESTER;
                            break;
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    switch (action) {
                        case READ_FACULTY:
                            subjectModel.setFaculty(Integer.parseInt(xmlStreamReader.getText()));
                            break;
                        case READ_SUBJECT:
                            subjectModel.setName(xmlStreamReader.getText());
                            break;
                        case READ_SEMESTER:
                            subjectModel.setSemester(Integer.parseInt(xmlStreamReader.getText()));
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    // save model to list
                    if (xmlStreamReader.getLocalName().equals("Subject")) {
                        subjectSet.add(subjectModel);
                    }
                    action = ActionType.SKIP;
                    break;

                default:
                    break;
            }
        }

        closeInputStream();
    }

    private enum ActionType {
        SKIP, READ_FACULTY, READ_SUBJECT, READ_SEMESTER
    }
}

