package su.logix.studperformance.main.Models;

import javafx.beans.property.SimpleStringProperty;
import su.logix.studperformance.main.connection.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class KeyModel {
    private final SimpleStringProperty userLogin;
    private final SimpleStringProperty userPass;
    private final SimpleStringProperty headKey;
    private final SimpleStringProperty adminKey;

    public KeyModel() {
        this.userLogin = new SimpleStringProperty();
        this.userPass = new SimpleStringProperty();
        this.headKey = new SimpleStringProperty();
        this.adminKey = new SimpleStringProperty();
    }

    public KeyModel(String userLogin, String userPass, String headKey, String adminKey) {
        this.userLogin = new SimpleStringProperty(userLogin);
        this.userPass = new SimpleStringProperty(userPass);
        this.headKey = new SimpleStringProperty(headKey);
        this.adminKey = new SimpleStringProperty(adminKey);
    }

    public static void setNewHeadKey(String newHeadKey) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE key SET value=? WHERE name='head_key'")) {
            pstmt.setString(1, newHeadKey);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось изменить запись в базе данных");
            }
        }
    }

    public static void setNewAdminKey(String newAdminKey) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE key SET value=? WHERE name='admin_key'")) {
            pstmt.setString(1, newAdminKey);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось изменить запись в базе данных");
            }
        }
    }

    public static void setNewUserLogin(String login) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE key SET value=? WHERE name='user_login'")) {
            pstmt.setString(1, login);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось изменить запись в базе данных");
            }
        }

    }

    public static void setNewUserPass(String pass) throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (PreparedStatement pstmt = connection.getPreparedStatement("UPDATE key SET value=? WHERE name='user_pass'")) {
            pstmt.setString(1, pass);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Не удалось изменить запись в базе данных");
            }
        }
    }

    public void loadExportKeys() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (Statement stmt = connection.getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT name, value FROM key WHERE name='head_key'")) {
            if (resultSet.next()) {
                setHeadKey(resultSet.getString("value"));
            }
        }
    }

    public void loadAuthKeys() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (Statement stmt = connection.getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT name, value FROM key WHERE name='user_login' OR name='user_pass'")) {
            while (resultSet.next()) {
                switch (resultSet.getString("name")) {
                    case "user_login":
                        setUserLogin(resultSet.getString("value"));
                        break;
                    case "user_pass":
                        setUserPass(resultSet.getString("value"));
                        break;
                }
            }
        }
    }

    public void loadAdminKey() throws SQLException {
        DBConnection connection = DBConnection.getInstance();
        try (Statement stmt = connection.getStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT name, value FROM key WHERE name='admin_key'")) {
            if (resultSet.next()) {
                setAdminKey(resultSet.getString("value"));
            }
        }
    }

    public String getUserLogin() {
        return userLogin.get();
    }

    public void setUserLogin(String userLogin) {
        this.userLogin.set(userLogin);
    }

    public String getUserPass() {
        return userPass.get();
    }

    public void setUserPass(String userPass) {
        this.userPass.set(userPass);
    }

    public String getHeadKey() {
        return headKey.get();
    }

    public void setHeadKey(String headKey) {
        this.headKey.set(headKey);
    }

    public String getAdminKey() {
        return adminKey.get();
    }

    public void setAdminKey(String adminKey) {
        this.adminKey.set(adminKey);
    }
}
