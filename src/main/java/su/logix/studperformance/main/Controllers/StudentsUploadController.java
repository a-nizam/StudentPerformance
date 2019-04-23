package su.logix.studperformance.main.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.logix.studperformance.main.Core.UploadController;
import su.logix.studperformance.main.Models.StudentModel;
import su.logix.studperformance.main.connection.DBConnection;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentsUploadController extends UploadController {

    private static final Logger log = Logger.getLogger(StudentsUploadController.class);

    @FXML
    public TextField tfFile;

    @FXML
    public void load() {
        if (tfFile.getText() != null && !tfFile.getText().trim().isEmpty()) {
            List<StudentModel> studentsList = new ArrayList<>();
            loadStudentsFromXML(tfFile.getText(), studentsList);
            saveStudentsToDB(studentsList);
        }
    }

    private void saveStudentsToDB(@NotNull List<StudentModel> studentsList) {
        DBConnection connection = DBConnection.getInstance();
        boolean oldAutoCommitValue = false;
        try {
            if (oldAutoCommitValue = connection.getAutoCommitValue()) {
                connection.disableAutoCommit();
            }
            StudentModel.setSemestersToZero();
            for (StudentModel studentModel : studentsList) {
                studentModel.save();
            }
            connection.commit();
            showMessage(Alert.AlertType.INFORMATION, "Выполнено", "Студенты загружены");

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                log.error("rollback error", e);
            }
            e.printStackTrace();
            log.error("save student to db error", e);
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

    @FXML
    public void showFileDialog() {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        showFileDialog(tfFile, extFilter);
    }

    private void loadStudentsFromXML(String filePath, List<StudentModel> studentsList) {
        try {
            XMLStreamReader xmlStreamReader = getXmlStreamReader(filePath);
            int event;
            ActionType action = ActionType.SKIP;
            String localName;
            StudentModel studentModel = new StudentModel();
            while (xmlStreamReader.hasNext()) {
                event = xmlStreamReader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        localName = xmlStreamReader.getLocalName();
                        switch (localName) {
                            case "Student":
                                studentModel = new StudentModel();
                                action = ActionType.SKIP;
                                break;
                            case "Surname":
                                action = ActionType.READ_SURNAME;
                                break;
                            case "Name":
                                action = ActionType.READ_NAME;
                                break;
                            case "SecName":
                                action = ActionType.READ_SECNAME;
                                break;
                            case "FacultyСode":
                                action = ActionType.READ_FACULTY;
                                break;
                            case "Group":
                                action = ActionType.READ_GROUP;
                                break;
                            case "Semester":
                                action = ActionType.READ_SEMESTER;
                                break;
                            case "RecordBookNumber":
                                action = ActionType.READ_RECORD_BOOK_NUM;
                                break;
                            default:
                                action = ActionType.SKIP;
                                break;
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        switch (action) {
                            case READ_SURNAME:
                                studentModel.setFam(xmlStreamReader.getText());
                                break;
                            case READ_NAME:
                                studentModel.setName(xmlStreamReader.getText());
                                break;
                            case READ_SECNAME:
                                studentModel.setSecname(xmlStreamReader.getText());
                                break;
                            case READ_FACULTY:
                                studentModel.setFaculty(Integer.parseInt(xmlStreamReader.getText()));
                                break;
                            case READ_GROUP:
                                studentModel.setGroup(xmlStreamReader.getText());
                                break;
                            case READ_SEMESTER:
                                studentModel.setSemester(Integer.parseInt(xmlStreamReader.getText()));
                                break;
                            case READ_RECORD_BOOK_NUM:
                                studentModel.setNzach(xmlStreamReader.getText());
                                break;
                            default:
                                break;
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        // save model to list
                        if (xmlStreamReader.getLocalName().equals("Student")) {
                            studentsList.add(studentModel);
                        }
                        action = ActionType.SKIP;
                        break;

                    default:
                        break;
                }
            }
            closeInputStream();
        } catch (XMLStreamException e) {
            e.printStackTrace();
            log.error("xml stream reader error", e);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("close input stream error", e);
        }
    }

    private enum ActionType {
        SKIP, READ_SURNAME, READ_NAME, READ_SECNAME, READ_FACULTY, READ_GROUP, READ_SEMESTER, READ_RECORD_BOOK_NUM
    }
}
