package su.logix.studperformance.main.Models;

import javafx.beans.property.SimpleStringProperty;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class OptionsModel {
    private final SimpleStringProperty department;
    private final SimpleStringProperty responsibleName;
    private final SimpleStringProperty headName;

    public OptionsModel() {
        this.department = new SimpleStringProperty();
        this.responsibleName = new SimpleStringProperty();
        this.headName = new SimpleStringProperty();
    }

    public static Map<Integer, String> getDates() throws SQLException {
        Map<Integer, String> dates = new HashMap<>();
        DBConnection connection = DBConnection.getInstance();
        try (Statement stmt = connection.getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT name, value FROM option WHERE name='date1' OR name='date2' OR name='date3'")) {
            while (resultSet.next()) {
                switch (resultSet.getString("name")) {
                    case "date1":
                        dates.put(1, resultSet.getString("value"));
                        break;
                    case "date2":
                        dates.put(2, resultSet.getString("value"));
                        break;
                    case "date3":
                        dates.put(3, resultSet.getString("value"));
                        break;
                }
            }
        }
        return dates;
    }

    public static void setDate(int date, String value) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE option SET value=? WHERE name=?")) {
            pstmt.setString(1, value);
            pstmt.setString(2, "date" + date);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось сохранить срок аттестации");
            }
        }
    }

    public void save() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        boolean oldAutoCommitValue = false;
        try {
            oldAutoCommitValue = connection.getAutoCommitValue();
            connection.disableAutoCommit();
            try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE option SET value=? WHERE name='department'")) {
                pstmt.setString(1, getDepartment());
                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("Не удалось сохранить настройки");
                }
            }
            try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE option SET value=? WHERE name='responsible_name'")) {
                pstmt.setString(1, getResponsibleName());
                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("Не удалось сохранить настройки");
                }
            }
            try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE option SET value=? WHERE name='head_name'")) {
                pstmt.setString(1, getHeadName());
                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("Не удалось сохранить настройки");
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            throw new SQLException(e.getMessage());
        } finally {
            if (oldAutoCommitValue) {
                try {
                    connection.enableAutoCommit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void load() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (Statement stmt = connection.getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT name, value FROM option")) {
            while (resultSet.next()) {
                switch (resultSet.getString("name")) {
                    case "department":
                        this.setDepartment(resultSet.getString("value"));
                        break;
                    case "responsible_name":
                        this.setResponsibleName(resultSet.getString("value"));
                        break;
                    case "head_name":
                        this.setHeadName(resultSet.getString("value"));
                        break;
                }
            }
        }
    }

    public String getDepartment() {
        return this.department.get();
    }

    public void setDepartment(String department) {
        this.department.set(department);
    }

    public String getResponsibleName() {
        return this.responsibleName.get();
    }

    public void setResponsibleName(String responsibleName) {
        this.responsibleName.set(responsibleName);
    }

    public String getHeadName() {
        return this.headName.get();
    }

    public void setHeadName(String headName) {
        this.headName.set(headName);
    }
}
