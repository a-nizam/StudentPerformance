package su.logix.studperformance.main.Models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CertificationModel {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty semester;
    private final SimpleIntegerProperty num;
    private final SimpleStringProperty year;
    private final SimpleIntegerProperty faculty;
    private final SimpleStringProperty group;
    private final SimpleIntegerProperty subject;
    private final SimpleBooleanProperty exported;
    private final SimpleListProperty<StudentsListModel> result;

    public CertificationModel(int id, int semester, int num, String year, int faculty, String group, int subject, boolean exported, ObservableList<StudentsListModel> result) {
        this.id = new SimpleIntegerProperty(id);
        this.semester = new SimpleIntegerProperty(semester);
        this.num = new SimpleIntegerProperty(num);
        this.year = new SimpleStringProperty(year);
        this.faculty = new SimpleIntegerProperty(faculty);
        this.group = new SimpleStringProperty(group);
        this.subject = new SimpleIntegerProperty(subject);
        this.exported = new SimpleBooleanProperty(exported);
        this.result = new SimpleListProperty<>(result);
    }

    public CertificationModel() {
        this.id = new SimpleIntegerProperty();
        this.semester = new SimpleIntegerProperty();
        this.num = new SimpleIntegerProperty();
        this.year = new SimpleStringProperty();
        this.faculty = new SimpleIntegerProperty();
        this.group = new SimpleStringProperty();
        this.subject = new SimpleIntegerProperty();
        this.exported = new SimpleBooleanProperty();
        this.result = new SimpleListProperty<>();
    }

    public static List<CertificationModel> getListBySubject(String year, int faculty, int semester, int certNum, int subject) throws SQLException {
        List<CertificationModel> list = new ArrayList<>();
        try (PreparedStatement pstmt = DBConnection.getInstance().getPreparedStatement("SELECT cert_id, cert_group," +
                " cert_exported FROM certification WHERE cert_year=? AND cert_faculty=? AND cert_semester=? AND cert_num=? AND cert_subject=?")) {
            pstmt.setString(1, year);
            pstmt.setInt(2, faculty);
            pstmt.setInt(3, semester);
            pstmt.setInt(4, certNum);
            pstmt.setInt(5, subject);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                CertificationModel certificationModel = new CertificationModel(resultSet.getInt("cert_id"), semester,
                        certNum, year, faculty, resultSet.getString("cert_group"), subject,
                        resultSet.getBoolean("cert_exported"), FXCollections.observableArrayList());
                certificationModel.initResult();
                list.add(certificationModel);
            }
        }
        return list;
    }

    public static List<String> getAllYears() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getInstance().getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT DISTINCT cert_year FROM certification")) {
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        }
        return list;
    }

    public static List<Integer> getAllCertNums() throws SQLException {
        List<Integer> list = new ArrayList<>();
        try (Statement stmt = DBConnection.getInstance().getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT DISTINCT cert_num FROM certification ORDER BY cert_num ASC")) {
            while (resultSet.next()) {
                list.add(resultSet.getInt(1));
            }
        }
        return list;
    }

    public static int exists(int certNum, String year, int faculty, int semester, String group, int subject) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("SELECT cert_id FROM certification WHERE cert_num=?" +
                " AND cert_year=? AND cert_faculty=? AND cert_semester=? AND cert_group=? AND cert_subject=?")) {
            pstmt.setInt(1, certNum);
            pstmt.setString(2, year);
            pstmt.setInt(3, faculty);
            pstmt.setInt(4, semester);
            pstmt.setString(5, group);
            pstmt.setInt(6, subject);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    public void save() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("INSERT INTO certification (cert_semester, cert_num, " +
                "cert_year, cert_faculty, cert_group, cert_subject, cert_exported) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, getSemester());
            pstmt.setInt(2, getNum());
            pstmt.setString(3, getYear());
            pstmt.setInt(4, getFaculty());
            pstmt.setString(5, getGroup());
            pstmt.setInt(6, getSubject());
            pstmt.setBoolean(7, isExported());
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось создать аттестационный лист");
            }

            int certificationId = -1;
            try (ResultSet resultSet = pstmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    certificationId = resultSet.getInt(1);
                }
            }

            if (certificationId == -1) {
                throw new SQLException("Не удалось получить id аттестационного листа");
            }

            setId(certificationId);
        }
    }

    public void update() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE certification SET cert_semester=?, cert_num=?, " +
                " cert_year=?, cert_faculty=?, cert_group=?, cert_subject=? WHERE cert_id=?")) {
            pstmt.setInt(1, getSemester());
            pstmt.setInt(2, getNum());
            pstmt.setString(3, getYear());
            pstmt.setInt(4, getFaculty());
            pstmt.setString(5, getGroup());
            pstmt.setInt(6, getSubject());
            pstmt.setInt(7, getId());
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось обновить аттестационный лист");
            }
        }
    }

    public void setExportedAndSave(boolean exported) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE certification SET cert_exported=? WHERE cert_id=?")) {
            pstmt.setBoolean(1, exported);
            pstmt.setInt(2, getId());
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось установить статус выгрузки аттестационного листа");
            }
        }
    }

    public void initResult() throws SQLException {
        this.setResult(FXCollections.observableArrayList());
        ObservableList<StudentsListModel> list = this.getResult();
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("SELECT res_id, ( stud_fam || \" \" || stud_name || \" \" || IFNULL(stud_secname, '')) AS name," +
                "stud_id,res_mark,res_practice_missed,res_practice_cor,res_lectures_missed,res_lectures_cor " +
                "FROM result LEFT JOIN student ON res_student = stud_id " +
                "WHERE res_certification = (SELECT cert_id FROM certification " +
                "WHERE cert_year = ? AND cert_faculty = ? AND cert_semester = ? AND cert_group = ? AND cert_num = ? AND cert_subject = ? )")) {
            setParams(pstmt);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                int counter = 1;
                while (resultSet.next()) {
                    list.add(new StudentsListModel(resultSet.getInt("res_id"), counter++, resultSet.getString("name"), resultSet.getInt("stud_id"),
                            resultSet.getFloat("res_mark"), resultSet.getInt("res_practice_missed"), resultSet.getInt("res_practice_cor"),
                            resultSet.getInt("res_lectures_missed"), resultSet.getInt("res_lectures_cor")));
                }
            }
        }
    }

    public void initIsExported() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("SELECT cert_exported FROM certification " +
                "WHERE cert_year = ? AND cert_faculty = ? AND cert_semester = ? AND cert_group = ? AND cert_num = ? AND cert_subject = ?")) {
            setParams(pstmt);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    setExported(resultSet.getBoolean(1));
                }
            }
        }
    }

    private void setParams(@NotNull PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, getYear());
        pstmt.setInt(2, getFaculty());
        pstmt.setInt(3, getSemester());
        pstmt.setString(4, getGroup());
        pstmt.setInt(5, getNum());
        pstmt.setInt(6, getSubject());
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getSemester() {
        return semester.get();
    }

    public void setSemester(int semester) {
        this.semester.set(semester);
    }

    public int getNum() {
        return num.get();
    }

    public void setNum(int num) {
        this.num.set(num);
    }

    public String getYear() {
        return year.get();
    }

    public void setYear(String year) {
        this.year.set(year);
    }

    public int getFaculty() {
        return faculty.get();
    }

    public void setFaculty(int faculty) {
        this.faculty.set(faculty);
    }

    public String getGroup() {
        return group.get();
    }

    public void setGroup(String group) {
        this.group.set(group);
    }

    public int getSubject() {
        return subject.get();
    }

    public void setSubject(int subject) {
        this.subject.set(subject);
    }

    public ObservableList<StudentsListModel> getResult() {
        return result.get();
    }

    public void setResult(ObservableList<StudentsListModel> result) {
        this.result.set(result);
    }

    public boolean isExported() {
        return exported.get();
    }

    public void setExported(boolean exported) {
        this.exported.set(exported);
    }
}
