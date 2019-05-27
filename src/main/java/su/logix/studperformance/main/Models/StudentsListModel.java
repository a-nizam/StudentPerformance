package su.logix.studperformance.main.Models;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentsListModel {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty num;
    private final SimpleStringProperty zachKn; // for export
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty studentId;
    private final SimpleFloatProperty mark;
    private final SimpleIntegerProperty practiceMissed;
    private final SimpleIntegerProperty practiceCorrected;
    private final SimpleIntegerProperty lecturesMissed;
    private final SimpleIntegerProperty lecturesCorrected;

    public StudentsListModel(int id, int num, String zachKn, String name, int studentId, float mark, int practiceMissed, int practiceCorrected, int lecturesMissed, int lecturesCorrected) {
        this.id = new SimpleIntegerProperty(id);
        this.num = new SimpleIntegerProperty(num);
        this.zachKn = new SimpleStringProperty(zachKn);
        this.name = new SimpleStringProperty(name);
        this.studentId = new SimpleIntegerProperty(studentId);
        this.mark = new SimpleFloatProperty(mark);
        this.practiceMissed = new SimpleIntegerProperty(practiceMissed);
        this.practiceCorrected = new SimpleIntegerProperty(practiceCorrected);
        this.lecturesMissed = new SimpleIntegerProperty(lecturesMissed);
        this.lecturesCorrected = new SimpleIntegerProperty(lecturesCorrected);
    }

    public void save(int certification) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("INSERT INTO result (res_student, res_certification, " +
                        "res_mark, res_lectures_missed, res_lectures_cor, res_practice_missed, res_practice_cor) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, getStudentId());
            pstmt.setInt(2, certification);
            pstmt.setFloat(3, getMark());
            pstmt.setInt(4, getLecturesMissed());
            pstmt.setInt(5, getLecturesCorrected());
            pstmt.setInt(6, getPracticeMissed());
            pstmt.setInt(7, getPracticeCorrected());
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось сохранить аттестационный лист");
            }
            int studentId = -1;
            try (ResultSet resultSet = pstmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    studentId = resultSet.getInt(1);
                }
            }
            if (studentId == -1) {
                throw new SQLException("Не удалось получить id студента");
            }
            setId(studentId);
        }
    }

    public void update() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE result SET res_mark=?, res_lectures_missed=?, " +
                "res_lectures_cor=?, res_practice_missed=?, res_practice_cor=? WHERE res_id=?")) {
            pstmt.setFloat(1, getMark());
            pstmt.setInt(2, getLecturesMissed());
            pstmt.setInt(3, getLecturesCorrected());
            pstmt.setInt(4, getPracticeMissed());
            pstmt.setInt(5, getPracticeCorrected());
            pstmt.setInt(6, getId());
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось обновить запись списка студентов (номер студента = " + getNum() + ")");
            }
        }
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getNum() {
        return num.get();
    }

    public void setNum(int num) {
        this.num.set(num);
    }

    public String getZachKn() {
        return zachKn.get();
    }

    public void setZachKn(String zachKn) {
        this.zachKn.set(zachKn);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getStudentId() {
        return this.studentId.get();
    }

    public void setStudentId(int studentId) {
        this.studentId.set(studentId);
    }

    public float getMark() {
        return mark.get();
    }

    public void setMark(float mark) {
        this.mark.set(mark);
    }

    public int getPracticeMissed() {
        return practiceMissed.get();
    }

    public void setPracticeMissed(int practiceMissed) {
        this.practiceMissed.set(practiceMissed);
    }

    public int getPracticeCorrected() {
        return practiceCorrected.get();
    }

    public void setPracticeCorrected(int practiceCorrected) {
        this.practiceCorrected.set(practiceCorrected);
    }

    public int getLecturesMissed() {
        return lecturesMissed.get();
    }

    public void setLecturesMissed(int lecturesMissed) {
        this.lecturesMissed.set(lecturesMissed);
    }

    public int getLecturesCorrected() {
        return lecturesCorrected.get();
    }

    public void setLecturesCorrected(int lecturesCorrected) {
        this.lecturesCorrected.set(lecturesCorrected);
    }
}
