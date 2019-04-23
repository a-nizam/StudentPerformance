package su.logix.studperformance.main.Models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubjectModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty faculty;
    private final SimpleIntegerProperty semester;

    public SubjectModel() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.faculty = new SimpleIntegerProperty();
        this.semester = new SimpleIntegerProperty();
    }

    public SubjectModel(int id, String name, int faculty, int semester) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.faculty = new SimpleIntegerProperty(faculty);
        this.semester = new SimpleIntegerProperty(semester);
    }

    public static List<SubjectModel> getListByFacultyAndSemester(int faculty, int semester) throws SQLException {
        List<SubjectModel> list = new ArrayList<>();
        String query = String.format("SELECT sub_id, sub_name FROM subject WHERE sub_faculty=%d AND sub_semester=%d ORDER BY sub_name ASC", faculty, semester);
        try (Statement stmt = DBConnection.getInstance().getStatement()) {
            ResultSet resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                list.add(new SubjectModel(resultSet.getInt("sub_id"), resultSet.getString("sub_name"),
                        faculty, semester));
            }
        }
        return list;
    }

    public static SubjectModel getById(int id) throws SQLException {
        SubjectModel subjectModel;
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("SELECT sub_name, sub_faculty, sub_semester FROM subject WHERE sub_id=?")) {
            pstmt.setInt(1, id);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    subjectModel = new SubjectModel(id, resultSet.getString("sub_name"), resultSet.getInt("sub_faculty"),
                            resultSet.getInt("sub_semester"));
                } else {
                    throw new SQLException("Не удалось получить модель предмета по ID");
                }
            }
        }
        return subjectModel;
    }

    public void save() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("INSERT OR IGNORE INTO subject (sub_name, " +
                "sub_faculty, sub_semester) VALUES(?, ?, ?)")) {
            pstmt.setString(1, getName());
            pstmt.setInt(2, getFaculty());
            pstmt.setInt(3, getSemester());
            pstmt.executeUpdate();
        }
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getSemester() {
        return semester.get();
    }

    public void setSemester(int semester) {
        this.semester.set(semester);
    }

    public int getFaculty() {
        return faculty.get();
    }

    public void setFaculty(int faculty) {
        this.faculty.set(faculty);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectModel that = (SubjectModel) o;
        return getName().equals(that.getName()) &&
                getFaculty() == that.getFaculty() &&
                getSemester() == that.getSemester();
    }

    @Override
    public int hashCode() {
        return getName().hashCode() * getFaculty() * getSemester();
    }
}
