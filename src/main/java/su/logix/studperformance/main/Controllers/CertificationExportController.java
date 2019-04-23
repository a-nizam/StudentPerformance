package su.logix.studperformance.main.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.docx4j.Docx4J;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jetbrains.annotations.NotNull;
import su.logix.studperformance.main.Core.FieldsController;
import su.logix.studperformance.main.Models.*;
import su.logix.studperformance.main.connection.DBConnection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CertificationExportController extends FieldsController {

    private static final Logger log = Logger.getLogger(CertificationExportController.class);

    @FXML
    public ComboBox<FacultyModel> cbFaculty;
    @FXML
    public ComboBox<Integer> cbSemester;
    @FXML
    public ComboBox<String> cbYear;
    @FXML
    public ComboBox<String> cbGroup;
    @FXML
    public ComboBox<Integer> cbCertNum;
    @FXML
    public ComboBox<SubjectModel> cbSubject;
    @FXML
    public PasswordField pfHeadKey;

    private ObservableList<String> yearsList;
    private ObservableList<Integer> certNumsList;
    private OutputStream fileOutputStream;

    @FXML
    public void initialize() {
        initYears();
        initCertNums();
        super.initFaculties(cbFaculty);
        super.initSemesters(cbSemester);
        super.initGroups(cbGroup);
        super.initSubjects(cbSubject);
    }

    private void initCertNums() {
        certNumsList = FXCollections.observableArrayList();
        try {
            certNumsList.addAll(CertificationModel.getAllCertNums());
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("loading certification numbers error", e);
        }
        cbCertNum.setItems(certNumsList);
    }

    private void initYears() {
        yearsList = FXCollections.observableArrayList();
        try {
            yearsList.addAll(CertificationModel.getAllYears());
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("loading years error", e);
        }
        cbYear.setItems(yearsList);
    }

    @FXML
    public void facultyChanged() {
        super.loadGroups(cbFaculty, cbSemester);
        super.loadSubjects(cbFaculty, cbSemester);
    }

    @FXML
    public void semesterChanged() {
        super.loadGroups(cbFaculty, cbSemester);
        super.loadSubjects(cbFaculty, cbSemester);
    }

    @FXML
    public void yearChanged() {

    }

    @FXML
    public void groupChanged() {

    }

    @FXML
    public void certNumChanged() {

    }

    @FXML
    public void subjectChanged() {

    }

    private XMLStreamWriter getXmlStreamWriter(String filePath) throws XMLStreamException {
        try {
            fileOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        } catch (FileNotFoundException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось найти файл");
            e.printStackTrace();
            log.error("file not found error", e);
        }
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        return xmlOutputFactory.createXMLStreamWriter(fileOutputStream, "UTF-8");
    }

    private void closeOutputStream() throws IOException {
        fileOutputStream.close();
    }

    private File showSaveFileDialog(FileChooser.ExtensionFilter extFilter) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранение файла");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showSaveDialog(cbFaculty.getScene().getWindow());
    }

    @FXML
    public void print() {
        if (cbYear.getSelectionModel().isEmpty() || cbFaculty.getSelectionModel().isEmpty() || cbSemester.getSelectionModel().isEmpty()
                || cbCertNum.getSelectionModel().isEmpty() || cbSubject.getSelectionModel().isEmpty() || cbGroup.getSelectionModel().isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Предупреждение", "Заполнены не все поля необходимые для печати аттестационного листа");
        } else {
            File file = showSaveFileDialog(new FileChooser.ExtensionFilter("Файлы документов (*.docx)", "*.docx"));
            if (file != null) {
                CertificationModel certificationModel = createModelFromFields();
                createXmlForDocument(certificationModel, "values.xml");
                combineDocx("template.docx", "values.xml", file);
            }
        }
    }

    private void createXmlForDocument(@NotNull CertificationModel certificationModel, String fileName) {
        OptionsModel optionsModel = new OptionsModel();
        try {
            optionsModel.load();
            XMLStreamWriter xmlStreamWriter = getXmlStreamWriter(fileName);
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeStartElement("certData");
            xmlStreamWriter.writeStartElement("faculty");
            xmlStreamWriter.writeCharacters(FacultyModel.getByCode(certificationModel.getFaculty()).getName());
            xmlStreamWriter.writeEndElement(); // faculty
            xmlStreamWriter.writeStartElement("semester");
            xmlStreamWriter.writeCharacters(Integer.toString(certificationModel.getSemester()));
            xmlStreamWriter.writeEndElement(); // semester
            xmlStreamWriter.writeStartElement("group");
            xmlStreamWriter.writeCharacters(certificationModel.getGroup());
            xmlStreamWriter.writeEndElement(); // group
            xmlStreamWriter.writeStartElement("year");
            xmlStreamWriter.writeCharacters(certificationModel.getYear());
            xmlStreamWriter.writeEndElement(); // year
            xmlStreamWriter.writeStartElement("certNum");
            xmlStreamWriter.writeCharacters(Integer.toString(certificationModel.getNum()));
            xmlStreamWriter.writeEndElement(); // certNum
            xmlStreamWriter.writeStartElement("department");
            xmlStreamWriter.writeCharacters(optionsModel.getDepartment());
            xmlStreamWriter.writeEndElement(); // department
            xmlStreamWriter.writeStartElement("subject");
            xmlStreamWriter.writeCharacters(SubjectModel.getById(certificationModel.getSubject()).getName());
            xmlStreamWriter.writeEndElement(); // subject
            xmlStreamWriter.writeStartElement("studentList");
            int counter = 1;
            for (StudentsListModel studentsListModel : certificationModel.getResult()) {
                xmlStreamWriter.writeStartElement("student");
                xmlStreamWriter.writeStartElement("number");
                xmlStreamWriter.writeCharacters(Integer.toString(counter++));
                xmlStreamWriter.writeEndElement(); // number
                xmlStreamWriter.writeStartElement("name");
                xmlStreamWriter.writeCharacters(studentsListModel.getName());
                xmlStreamWriter.writeEndElement(); // name
                xmlStreamWriter.writeStartElement("mark");
                xmlStreamWriter.writeCharacters(Float.toString(studentsListModel.getMark()));
                xmlStreamWriter.writeEndElement(); // mark
                xmlStreamWriter.writeStartElement("practiceMissed");
                xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getPracticeMissed()));
                xmlStreamWriter.writeEndElement(); // practiceMissed
                xmlStreamWriter.writeStartElement("practiceCorrected");
                xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getPracticeCorrected()));
                xmlStreamWriter.writeEndElement(); // practiceCorrected
                xmlStreamWriter.writeStartElement("lecturesMissed");
                xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getLecturesMissed()));
                xmlStreamWriter.writeEndElement(); // lecturesMissed
                xmlStreamWriter.writeStartElement("lecturesCorrected");
                xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getLecturesCorrected()));
                xmlStreamWriter.writeEndElement(); // lecturesCorrected
                xmlStreamWriter.writeEndElement(); // student
            }
            xmlStreamWriter.writeEndElement(); // studentList
            xmlStreamWriter.writeStartElement("responsible");
            xmlStreamWriter.writeCharacters(optionsModel.getResponsibleName());
            xmlStreamWriter.writeEndElement(); // responsible
            xmlStreamWriter.writeStartElement("head");
            xmlStreamWriter.writeCharacters(optionsModel.getHeadName());
            xmlStreamWriter.writeEndElement(); // head
            xmlStreamWriter.writeStartElement("date");
            xmlStreamWriter.writeCharacters(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
            xmlStreamWriter.writeEndElement(); // date
            xmlStreamWriter.writeEndElement(); // certData
            xmlStreamWriter.writeEndDocument();
            closeOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("close output stream error", e);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            log.error("xml stream writer error", e);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("options model load error", e);
        }
    }

    private void combineDocx(String docx, String xml, File resultFile) {
        try {
            WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(docx));
            FileInputStream xmlStream = new FileInputStream(new File(xml));
            BindingHandler.getHyperlinkResolver().setHyperlinkStyle("Hyperlink");
            Docx4J.bind(wordMLPackage, xmlStream, Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML);
            Docx4J.save(wordMLPackage, resultFile, Docx4J.FLAG_NONE);
        } catch (FileNotFoundException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось найти файл: " + e.getMessage());
            e.printStackTrace();
            log.error("file not found error", e);
        } catch (Docx4JException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось создать документ: " + e.getMessage());
            e.printStackTrace();
            log.error("document create error", e);
        }
        showMessage(Alert.AlertType.INFORMATION, "Успешно", "Аттестационный лист создан: " + resultFile.getAbsolutePath());
    }

    private void setExported(@NotNull List<CertificationModel> list) {
        DBConnection connection = DBConnection.getInstance();
        boolean oldAutoCommitValue = false;
        try {
            if (oldAutoCommitValue = connection.getAutoCommitValue()) {
                connection.disableAutoCommit();
            }
            for (CertificationModel model : list) {
                model.setExportedAndSave(true);
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                log.error("rollback error", e);
            }
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось установить статус выгрузки аттестационных листов");
            e.printStackTrace();
            log.error("set export status error", e);
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
    public void export() {
        if (cbYear.getSelectionModel().isEmpty() ||
                cbFaculty.getSelectionModel().isEmpty() ||
                cbSemester.getSelectionModel().isEmpty() ||
                cbCertNum.getSelectionModel().isEmpty() ||
                cbSubject.getSelectionModel().isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Предупреждение", "Заполните все поля");
        } else {
            if (pfHeadKey.getText().isEmpty()) {
                showMessage(Alert.AlertType.WARNING, "Предупреждение", "Введите ключ");
            } else {
                if (checkKeys()) {
                    List<CertificationModel> certificationModelList = null;
                    try {
                        certificationModelList = CertificationModel.getListBySubject(cbYear.getValue(),
                                cbFaculty.getValue().getCode(), cbSemester.getValue(), cbCertNum.getValue(), cbSubject.getValue().getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        log.error("certification load list by subject error", e);
                    }
                    File file = showSaveFileDialog(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));
                    if (file != null) {
                        if (certificationModelList != null) {
                            createXml(certificationModelList, file.getAbsolutePath());
                            setExported(certificationModelList);
                            showMessage(Alert.AlertType.INFORMATION, "Успешно", "Файл сохранен " + file.getAbsolutePath());
                        } else {
                            showMessage(Alert.AlertType.ERROR, "Ошибка", "Возникла ошибка при получении аттестационных листов");
                            log.error("certification export error: CertificationModel.getListBySubject returns null");
                        }
                    }
                } else {
                    showMessage(Alert.AlertType.WARNING, "Предупреждение", "Неверные ключи");
                }
            }
        }
    }

    /**
     * @return true if keys are true
     */
    private boolean checkKeys() {
        try {
            KeyModel keyModel = new KeyModel();
            keyModel.loadExportKeys();
            if (getHash(pfHeadKey.getText()).equals(keyModel.getHeadKey())) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createXml(@NotNull List<CertificationModel> certificationModelList, String name) {
        try {
            XMLStreamWriter xmlStreamWriter = getXmlStreamWriter(name);
            OptionsModel optionsModel = new OptionsModel();
            optionsModel.load();
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeStartElement("Ведомости");
            for (CertificationModel certificationModel : certificationModelList) {
                xmlStreamWriter.writeStartElement("Ведомость");
                xmlStreamWriter.writeStartElement("УчебныйГод");
                xmlStreamWriter.writeCharacters(certificationModel.getYear());
                xmlStreamWriter.writeEndElement(); // УчебныйГод
                xmlStreamWriter.writeStartElement("Дисциплина");
                xmlStreamWriter.writeCharacters(SubjectModel.getById(certificationModel.getSubject()).getName());
                xmlStreamWriter.writeEndElement(); // Дисциплина
                xmlStreamWriter.writeStartElement("Факультет");
                xmlStreamWriter.writeCharacters(Integer.toString(certificationModel.getFaculty()));
                xmlStreamWriter.writeEndElement(); // Факультет
                xmlStreamWriter.writeStartElement("Кафедра");
                xmlStreamWriter.writeCharacters(optionsModel.getDepartment());
                xmlStreamWriter.writeEndElement(); // Кафедра
                xmlStreamWriter.writeStartElement("ПериодКонтроля");
                xmlStreamWriter.writeCharacters(Integer.toString(certificationModel.getSemester()));
                xmlStreamWriter.writeEndElement(); // ПериодКонтроля
                xmlStreamWriter.writeStartElement("Группа");
                xmlStreamWriter.writeCharacters(certificationModel.getGroup());
                xmlStreamWriter.writeEndElement(); // Группа
                xmlStreamWriter.writeStartElement("АттестацияНомер");
                xmlStreamWriter.writeCharacters(Integer.toString(certificationModel.getNum()));
                xmlStreamWriter.writeEndElement(); // АттестацияНомер
                xmlStreamWriter.writeStartElement("Ответственный");
                xmlStreamWriter.writeCharacters(optionsModel.getResponsibleName());
                xmlStreamWriter.writeEndElement(); // Ответственный
                xmlStreamWriter.writeStartElement("СписокСтудентов");
                for (StudentsListModel studentsListModel : certificationModel.getResult()) {
                    xmlStreamWriter.writeStartElement("ДанныеСтудента");
                    xmlStreamWriter.writeStartElement("ФИО");
                    xmlStreamWriter.writeCharacters(studentsListModel.getName());
                    xmlStreamWriter.writeEndElement(); // ФИО
                    xmlStreamWriter.writeStartElement("ПропускиЛекций");
                    xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getLecturesMissed()));
                    xmlStreamWriter.writeEndElement(); // ПропускиЛекций
                    xmlStreamWriter.writeStartElement("ПропускиПрактических");
                    xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getPracticeMissed()));
                    xmlStreamWriter.writeEndElement(); // ПропускиПрактических
                    xmlStreamWriter.writeStartElement("ОтработкиЛекций");
                    xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getLecturesCorrected()));
                    xmlStreamWriter.writeEndElement(); // ОтработкиЛекций
                    xmlStreamWriter.writeStartElement("ОтработкиПрактических");
                    xmlStreamWriter.writeCharacters(Integer.toString(studentsListModel.getPracticeCorrected()));
                    xmlStreamWriter.writeEndElement(); // ОтработкиПрактических
                    xmlStreamWriter.writeStartElement("СреднийБалл");
                    xmlStreamWriter.writeCharacters(Float.toString(studentsListModel.getMark()));
                    xmlStreamWriter.writeEndElement(); // СреднийБалл
                    xmlStreamWriter.writeEndElement(); // ДанныеСтудента
                }
                xmlStreamWriter.writeEndElement(); // СписокСтудентов
                xmlStreamWriter.writeEndElement(); // Ведомость
            }
            xmlStreamWriter.writeEndElement(); // Ведомости
            xmlStreamWriter.writeEndDocument();
            closeOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("close output stream error", e);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            log.error("xml stream writer error", e);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("options model load error", e);
        }
    }

    private CertificationModel createModelFromFields() {
        CertificationModel certificationModel = new CertificationModel();
        certificationModel.setYear(cbYear.getValue());
        certificationModel.setFaculty(cbFaculty.getValue().getCode());
        certificationModel.setSemester(cbSemester.getValue());
        certificationModel.setGroup(cbGroup.getValue());
        certificationModel.setNum(cbCertNum.getValue());
        certificationModel.setSubject(cbSubject.getValue().getId());
        try {
            certificationModel.initResult();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("certification model load error", e);
        }
        return certificationModel;
    }

    public void showCertification() {
        if (cbYear.getSelectionModel().isEmpty() || cbFaculty.getSelectionModel().isEmpty() || cbSemester.getSelectionModel().isEmpty()
                || cbCertNum.getSelectionModel().isEmpty() || cbSubject.getSelectionModel().isEmpty() || cbGroup.getSelectionModel().isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Предупреждение", "Заполнены не все поля необходимые для просмотра аттестационного листа");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/certPreview.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Просмотр аттестационного листа");
                stage.setScene(new Scene(root));
                stage.show();

                CertPreviewController controller = loader.getController();
                controller.setYear(cbYear.getValue());
                controller.setFaculty(cbFaculty.getValue().getCode());
                controller.setSemester(cbSemester.getValue());
                controller.setCertNum(cbCertNum.getValue());
                controller.setSubject(cbSubject.getValue().getId());
                controller.setGroup(cbGroup.getValue());
                controller.initForm();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("loading certification preview form error", e);
            }
        }
    }
}
