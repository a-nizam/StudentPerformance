package su.logix.studperformance.main.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import su.logix.studperformance.main.Models.CertificationModel;
import su.logix.studperformance.main.Models.StudentsListModel;

import java.sql.SQLException;

public class CertPreviewController {

    private static final Logger log = Logger.getLogger(CertPreviewController.class);

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

    private CertificationModel certificationModel;
    private ObservableList<StudentsListModel> studentsList;

    private String year;
    private int faculty;
    private int semester;
    private int certNum;
    private int subject;
    private String group;

    void initForm() {
        certificationModel = getCertificationModel();
        initStudentsList();
        loadStudentsList();
        tableStudentsList.getSelectionModel().setCellSelectionEnabled(true);
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
        studentsList.clear();
        studentsList.addAll(certificationModel.getResult());
    }

    private CertificationModel getCertificationModel() {
        CertificationModel certificationModel = new CertificationModel();
        certificationModel.setYear(getYear());
        certificationModel.setFaculty(getFaculty());
        certificationModel.setSemester(getSemester());
        certificationModel.setGroup(getGroup());
        certificationModel.setNum(getCertNum());
        certificationModel.setSubject(getSubject());
        try {
            certificationModel.initResult();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("certification model initializing error", e);
        }
        return certificationModel;
    }

    private void configureColumnNum() {
        columnNum.setCellValueFactory(new PropertyValueFactory<>("num"));
    }

    private void configureColumnName() {
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    private void configureColumnMark() {
        columnMark.setCellValueFactory(new PropertyValueFactory<>("mark"));
        columnMark.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
    }

    private void configureColumnPracticeMissed() {
        columnPracticeMissed.setCellValueFactory(new PropertyValueFactory<>("practiceMissed"));
        columnPracticeMissed.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    }

    private void configureColumnPracticeCorrected() {
        columnPracticeCorrected.setCellValueFactory(new PropertyValueFactory<>("practiceCorrected"));
        columnPracticeCorrected.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    }

    private void configureColumnLecturesMissed() {
        columnLecturesMissed.setCellValueFactory(new PropertyValueFactory<>("lecturesMissed"));
        columnLecturesMissed.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    }

    private void configureColumnLecturesCorrected() {
        columnLecturesCorrected.setCellValueFactory(new PropertyValueFactory<>("lecturesCorrected"));
        columnLecturesCorrected.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    }

    @Contract(pure = true)
    private String getYear() {
        return year;
    }

    void setYear(String year) {
        this.year = year;
    }

    @Contract(pure = true)
    private int getFaculty() {
        return faculty;
    }

    void setFaculty(int faculty) {
        this.faculty = faculty;
    }

    @Contract(pure = true)
    private int getSemester() {
        return semester;
    }

    void setSemester(int semester) {
        this.semester = semester;
    }

    @Contract(pure = true)
    private int getCertNum() {
        return certNum;
    }

    void setCertNum(int certNum) {
        this.certNum = certNum;
    }

    @Contract(pure = true)
    private int getSubject() {
        return subject;
    }

    void setSubject(int subject) {
        this.subject = subject;
    }

    @Contract(pure = true)
    private String getGroup() {
        return group;
    }

    void setGroup(String group) {
        this.group = group;
    }

    @FXML
    public void close() {
        ((Stage) tableStudentsList.getScene().getWindow()).close();
    }
}
