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

public class FacultyModel {
    private final SimpleIntegerProperty code;
    private final SimpleStringProperty name;

    public FacultyModel(int code, String name) {
        this.code = new SimpleIntegerProperty(code);
        this.name = new SimpleStringProperty(name);
    }

    public static List<FacultyModel> getList() throws SQLException {
        List<FacultyModel> list = new ArrayList<>();
        String query = "SELECT fac_code, fac_name FROM faculty";
        try (
                Statement stmt = DBConnection.getInstance().getStatement();
                ResultSet resultSet = stmt.executeQuery(query)
        ) {
            while (resultSet.next()) {
                list.add(new FacultyModel(resultSet.getInt("fac_code"), resultSet.getString("fac_name")));
            }
        }
        return list;
    }

    public static FacultyModel getByCode(int code) throws SQLException {
        FacultyModel facultyModel;
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("SELECT fac_name FROM faculty WHERE fac_code=?")) {
            pstmt.setInt(1, code);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    facultyModel = new FacultyModel(code, resultSet.getString("fac_name"));
                } else {
                    throw new SQLException("Не удалось получить модель факультета по коду");
                }
            }
        }
        return facultyModel;
    }

    public int getCode() {
        return code.get();
    }

    public void setCode(int code) {
        this.code.set(code);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
}
