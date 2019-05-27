package su.logix.studperformance.main.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.log4j.Logger;
import su.logix.studperformance.main.Core.FieldsController;
import su.logix.studperformance.main.Models.*;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.SQLException;
import java.util.List;

public class SheetController extends FieldsController {

    private static final Logger log = Logger.getLogger(SheetController.class);

    @FXML
    public ComboBox<Integer> cbCertNum;
    @FXML
    public ComboBox<String> cbYear;
    @FXML
    public ComboBox<FacultyModel> cbFaculty;
    @FXML
    public ComboBox<Integer> cbSemester;
    @FXML
    public ComboBox<String> cbGroup;
    @FXML
    public ComboBox<SubjectModel> cbSubject;

    @FXML
    public TableView<StudentsListModel> tableStudentsList;
    @FXML
    public TableColumn<StudentsListModel, Integer> columnNum;
    @FXML
    public TableColumn<StudentsListModel, String> columnName;
    @FXML
    public TableColumn<StudentsListModel, Float> columnMark;
    @FXML
    public TableColumn<StudentsListModel, Integer> columnPracticeMissed;
    @FXML
    public TableColumn<StudentsListModel, Integer> columnPracticeCorrected;
    @FXML
    public TableColumn<StudentsListModel, Integer> columnLecturesMissed;
    @FXML
    public TableColumn<StudentsListModel, Integer> columnLecturesCorrected;

    private ObservableList<Integer> certNumsList;
    private ObservableList<String> yearList;
    private ObservableList<StudentsListModel> studentsList;

    /**
     * 0 if certification not exists, > 0 if certification exists
     */
    private int certId = 0;
    private CertificationModel certificationModel;

    @FXML
    public void initialize() {
        initCertNums();
        iniYears();
        super.initFaculties(cbFaculty);
        super.initSemesters(cbSemester);
        super.initGroups(cbGroup);
        super.initSubjects(cbSubject);
        initStudentsList();
        tableStudentsList.getSelectionModel().setCellSelectionEnabled(true);
    }

    @FXML
    public void certNumChanged() {
        if (!loadCertIfExisting()) {
            loadStudentsList();
        }
    }

    @FXML
    public void yearChanged() {
        if (!loadCertIfExisting()) {
            loadStudentsList();
        }
    }

    @FXML
    public void facultyChanged() {
        clearAllFields();
        super.loadGroups(cbFaculty, cbSemester);
        super.loadSubjects(cbFaculty, cbSemester);
    }

    @FXML
    public void semesterChanged() {
        clearAllFields();
        super.loadGroups(cbFaculty, cbSemester);
        super.loadSubjects(cbFaculty, cbSemester);
    }

    @FXML
    public void groupChanged() {
        if (!loadCertIfExisting()) {
            loadStudentsList();
        }
    }


    @FXML
    public void subjectChanged() {
        if (!loadCertIfExisting()) {
            loadStudentsList();
        }
    }

    /**
     * @return true if certification exists
     */
    private boolean loadCertIfExisting() {
        if (isCertListExists()) {
            certificationModel = getCertificationModel();
            loadExistingStudentsList();
            return true;
        }
        return false;
    }

    private void loadExistingStudentsList() {
        studentsList.clear();
        studentsList.addAll(certificationModel.getResult());
    }

    private CertificationModel getCertificationModel() {
        CertificationModel certificationModel = new CertificationModel();
        certificationModel.setYear(cbYear.getValue());
        certificationModel.setFaculty(cbFaculty.getValue().getCode());
        certificationModel.setSemester(cbSemester.getValue());
        certificationModel.setGroup(cbGroup.getValue());
        certificationModel.setNum(cbCertNum.getValue());
        certificationModel.setSubject(cbSubject.getValue().getId());
        try {
            certificationModel.initResult();
            certificationModel.initIsExported();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("certification model initializing error", e);
        }
        return certificationModel;
    }

    /**
     * Sets certId if certification exists
     *
     * @return true if certification exists
     */
    private boolean isCertListExists() {
        if (!cbCertNum.getSelectionModel().isEmpty() && !cbYear.getSelectionModel().isEmpty() && !cbFaculty.getSelectionModel().isEmpty()
                && !cbSemester.getSelectionModel().isEmpty() && !cbGroup.getSelectionModel().isEmpty() && !cbSubject.getSelectionModel().isEmpty()) {
            try {
                certId = CertificationModel.exists(cbCertNum.getValue(), cbYear.getValue(), cbFaculty.getValue().getCode(),
                        cbSemester.getValue(), cbGroup.getValue(), cbSubject.getValue().getId());
                if (certId > 0) {
                    return true;
                }
            } catch (SQLException e) {
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Ошибка проверки существования ведомости");
                e.printStackTrace();
                log.error("existing certification checking error", e);
            }
        }
        return false;
    }

    private void initCertNums() {
        certNumsList = FXCollections.observableArrayList();
        certNumsList.addAll(1, 2, 3);
        cbCertNum.setItems(certNumsList);
    }

    private void iniYears() {
        yearList = FXCollections.observableArrayList();
        yearList.addAll("2018-2019", "2019-2020", "2020-2021", "2021-2022", "2022-2023", "2023-2024", "2024-2025",
                "2025-2026", "2026-2027", "2027-2028", "2028-2029");
        cbYear.setItems(yearList);
    }

    private void initStudentsList() {
        configureColumnNum();
        configureColumnName();
        configureColumnMark();
        configureColumnPracticeMissed();
        configureColumnPracticeCorrected();
        configureColumnLecturesMissed();
        configureColumnLecturesCorrected();
        studentsList = FXCollections.observableArrayList();
        tableStudentsList.setItems(studentsList);
    }

    private void loadStudentsList() {
        if (!cbFaculty.getSelectionModel().isEmpty() && !cbSemester.getSelectionModel().isEmpty() && !cbGroup.getSelectionModel().isEmpty()) {
            studentsList.clear();
            try {
                List<StudentModel> studentInfoList = StudentModel.getList(cbFaculty.getValue().getCode(), cbSemester.getValue(), cbGroup.getValue());
                int i = 1;
                for (StudentModel studentModel : studentInfoList) {
                    studentsList.add(new StudentsListModel(0, i++, studentModel.getNzach(), studentModel.getFullName(),
                            studentModel.getId(), 0, 0, 0, 0, 0));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("student list loading error", e);
            }
        }
    }

    private void clearAllFields() {
        certId = 0;
        studentsList.clear();
    }

    private void configureColumnNum() {
        columnNum.setCellValueFactory(new PropertyValueFactory<>("num"));
    }

    private void configureColumnName() {
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    // TODO: ограничить балл
    private void configureColumnMark() {
        columnMark.setCellValueFactory(new PropertyValueFactory<>("mark"));
        columnMark.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        columnMark.setOnEditCommit(
                (TableColumn.CellEditEvent<StudentsListModel, Float> t) -> {
                    if (t.getNewValue() >= 0.0 && t.getNewValue() <= 5.0) {
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setMark(t.getNewValue());
                    } else {
                        showMessage(Alert.AlertType.WARNING, "Предупреждение", "Балл не может быть больше 5 и меньше 0");
                        t.getTableView().refresh();
                    }
                }
        );
    }

    private void configureColumnPracticeMissed() {
        columnPracticeMissed.setCellValueFactory(new PropertyValueFactory<>("practiceMissed"));
        columnPracticeMissed.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        columnPracticeMissed.setOnEditCommit(
                (TableColumn.CellEditEvent<StudentsListModel, Integer> t) ->
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setPracticeMissed(t.getNewValue())
        );
    }

    private void configureColumnPracticeCorrected() {
        columnPracticeCorrected.setCellValueFactory(new PropertyValueFactory<>("practiceCorrected"));
        columnPracticeCorrected.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        columnPracticeCorrected.setOnEditCommit(
                (TableColumn.CellEditEvent<StudentsListModel, Integer> t) ->
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setPracticeCorrected(t.getNewValue())
        );
    }

    private void configureColumnLecturesMissed() {
        columnLecturesMissed.setCellValueFactory(new PropertyValueFactory<>("lecturesMissed"));
        columnLecturesMissed.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        columnLecturesMissed.setOnEditCommit(
                (TableColumn.CellEditEvent<StudentsListModel, Integer> t) ->
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setLecturesMissed(t.getNewValue())
        );
    }

    private void configureColumnLecturesCorrected() {
        columnLecturesCorrected.setCellValueFactory(new PropertyValueFactory<>("lecturesCorrected"));
        columnLecturesCorrected.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        columnLecturesCorrected.setOnEditCommit(
                (TableColumn.CellEditEvent<StudentsListModel, Integer> t) ->
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setLecturesCorrected(t.getNewValue())
        );
    }

    @FXML
    public void save() {
        if (cbCertNum.getSelectionModel().isEmpty() || cbYear.getSelectionModel().isEmpty() || cbFaculty.getSelectionModel().isEmpty()
                || cbSemester.getSelectionModel().isEmpty() || cbGroup.getSelectionModel().isEmpty() || cbSubject.getSelectionModel().isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Предупреждение", "Заполнены не все поля");
        } else {
            DBConnection connection = DBConnection.getInstance();
            boolean oldAutoCommitValue = false;
            try {
                if (oldAutoCommitValue = connection.getAutoCommitValue()) {
                    connection.disableAutoCommit();
                }

                boolean exported = false;

                if (certId == 0) {
                    // saving certification list
                    certificationModel = new CertificationModel(0, cbSemester.getValue(),
                            cbCertNum.getValue(), cbYear.getValue(),
                            cbFaculty.getValue().getCode(), cbGroup.getValue(), cbSubject.getValue().getId(),
                            false, null);
                    int certificationId;
                    certificationModel.save();
                    certificationId = certificationModel.getId();

                    // saving students result
                    for (StudentsListModel studentsListModel : studentsList) {
                        studentsListModel.save(certificationId);
                    }
                    certId = certificationId;
                    exported = true;
                } else {
                    if (certificationModel.isExported()) {
                        showMessage(Alert.AlertType.WARNING, "Предупреждение", "Аттестационный лист был выгружен ранее");
                    } else {
                        // updating certification list
                        certificationModel.setId(certId);
                        certificationModel.setYear(cbYear.getValue());
                        certificationModel.setFaculty(cbFaculty.getValue().getCode());
                        certificationModel.setSemester(cbSemester.getValue());
                        certificationModel.setGroup(cbGroup.getValue());
                        certificationModel.setNum(cbCertNum.getValue());
                        certificationModel.setSubject(cbSubject.getValue().getId());
                        certificationModel.update();

                        // updating students result
                        for (StudentsListModel studentsListModel : studentsList) {
                            studentsListModel.update();
                        }
                        exported = true;
                    }
                }

                // commiting if everything is fine
                connection.commit();
                if (exported) {
                    showMessage(Alert.AlertType.INFORMATION, "Сохранение аттестационного листа", "Аттестационный лист успешно сохранен");
                    exported = false;
                }
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    log.error("rollback error", e);
                }
                showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить аттестационный лист");
                e.printStackTrace();
                log.error("certification save error", e);
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
    }
}
