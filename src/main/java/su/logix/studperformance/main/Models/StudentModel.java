package su.logix.studperformance.main.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nzach;
    private final SimpleStringProperty fam;
    private final SimpleStringProperty name;
    private final SimpleStringProperty secname;
    private final SimpleStringProperty fullName;
    private final SimpleIntegerProperty semester;
    private final SimpleStringProperty group;
    private final SimpleIntegerProperty faculty;

    public StudentModel() {
        this.id = new SimpleIntegerProperty();
        this.nzach = new SimpleStringProperty();
        this.fam = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.secname = new SimpleStringProperty();
        this.fullName = new SimpleStringProperty();
        this.semester = new SimpleIntegerProperty();
        this.group = new SimpleStringProperty();
        this.faculty = new SimpleIntegerProperty();
    }

    public StudentModel(int id, String nzach, String fam, String name, String secname, int semester, String group, int faculty) {
        this.id = new SimpleIntegerProperty(id);
        this.nzach = new SimpleStringProperty(nzach);
        this.fam = new SimpleStringProperty(fam);
        this.name = new SimpleStringProperty(name);
        this.secname = new SimpleStringProperty(secname);
        this.fullName = new SimpleStringProperty(String.format("%s %s %s", this.fam, this.name, this.secname));
        this.semester = new SimpleIntegerProperty(semester);
        this.group = new SimpleStringProperty(group);
        this.faculty = new SimpleIntegerProperty(faculty);
    }

    public StudentModel(int id, String nzach, String fam, String name, String secname, String fullName, int semester, String group, int faculty) {
        this.id = new SimpleIntegerProperty(id);
        this.nzach = new SimpleStringProperty(nzach);
        this.fam = new SimpleStringProperty(fam);
        this.name = new SimpleStringProperty(name);
        this.secname = new SimpleStringProperty(secname);
        this.fullName = new SimpleStringProperty(fullName);
        this.semester = new SimpleIntegerProperty(semester);
        this.group = new SimpleStringProperty(group);
        this.faculty = new SimpleIntegerProperty(faculty);
    }

    public static List<String> getGroupsByFacultyAndSemester(int faculty, int semester) throws SQLException {
        List<String> list = new ArrayList<>();
        PreparedStatement pstmt = DBConnection.getInstance().getPreparedStatement("SELECT DISTINCT stud_group FROM student WHERE stud_faculty=? AND stud_semester=? ORDER BY stud_group ASC");
        pstmt.setInt(1, faculty);
        pstmt.setInt(2, semester);
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            list.add(resultSet.getString(1));
        }
        return list;
    }

    public static List<StudentModel> getList(int faculty, int semester, String group) throws SQLException {
        List<StudentModel> list = new ArrayList<>();
        String query = "SELECT stud_id, stud_nzach, stud_fam, stud_name, IFNULL(stud_secname, '') AS stud_secname FROM student WHERE stud_faculty=? AND stud_semester=? AND stud_group=?";
        try (PreparedStatement pstmt = DBConnection.getInstance().getPreparedStatement(query)) {
            pstmt.setInt(1, faculty);
            pstmt.setInt(2, semester);
            pstmt.setString(3, group);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                list.add(new StudentModel(resultSet.getInt("stud_id"), resultSet.getString("stud_nzach"),
                        resultSet.getString("stud_fam"), resultSet.getString("stud_name"), resultSet.getString("stud_secname"),
                        String.format("%s %s %s", resultSet.getString("stud_fam"), resultSet.getString("stud_name"), resultSet.getString("stud_secname")),
                        semester, group, faculty));
            }
            return list;
        }
    }

    public static void setSemestersToZero() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (Statement stmt = connection.getStatement()) {
            stmt.executeUpdate("UPDATE student SET stud_semester=0");
        }
    }

    public void save() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("SELECT stud_id FROM student WHERE stud_nzach=?")) {
            pstmt.setString(1, getNzach());
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                try (PreparedStatement pstmtUpdate = connection.getPreparedStatement("UPDATE student SET " +
                        "stud_fam=?, stud_name=?, stud_secname=?, stud_semester=?, stud_group=?, stud_faculty=? WHERE stud_id=?")) {
                    pstmtUpdate.setString(1, getFam());
                    pstmtUpdate.setString(2, getName());
                    pstmtUpdate.setString(3, getSecname());
                    pstmtUpdate.setInt(4, getSemester());
                    pstmtUpdate.setString(5, getGroup());
                    pstmtUpdate.setInt(6, getFaculty());
                    pstmtUpdate.setInt(7, resultSet.getInt(1));
                    if (pstmtUpdate.executeUpdate() == 0) {
                        throw new SQLException("Не удалось сохранить студента");
                    }
                }
            } else {
                try (PreparedStatement pstmtInsert = connection.getPreparedStatement("INSERT INTO student (stud_nzach, stud_fam, " +
                        "stud_name, stud_secname, stud_semester, stud_group, stud_faculty) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
                    pstmtInsert.setString(1, getNzach());
                    pstmtInsert.setString(2, getFam());
                    pstmtInsert.setString(3, getName());
                    pstmtInsert.setString(4, getSecname());
                    pstmtInsert.setInt(5, getSemester());
                    pstmtInsert.setString(6, getGroup());
                    pstmtInsert.setInt(7, getFaculty());
                    if (pstmtInsert.executeUpdate() == 0) {
                        throw new SQLException("Не удалось сохранить студента");
                    }
                }
            }
        }
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNzach() {
        return nzach.get();
    }

    public void setNzach(String nzach) {
        this.nzach.set(nzach);
    }

    public String getFam() {
        return fam.get();
    }

    public void setFam(String fam) {
        this.fam.set(fam);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getSecname() {
        return secname.get();
    }

    public void setSecname(String secname) {
        this.secname.set(secname);
    }

    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public int getSemester() {
        return semester.get();
    }

    public void setSemester(int semester) {
        this.semester.set(semester);
    }

    public String getGroup() {
        return group.get();
    }

    public void setGroup(String group) {
        this.group.set(group);
    }

    public int getFaculty() {
        return faculty.get();
    }

    public void setFaculty(int faculty) {
        this.faculty.set(faculty);
    }
}
