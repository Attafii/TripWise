package ui.admin.repository.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    // Use your actual credentials here
    private static final String URL =
            "jdbc:mysql://localhost:3306/tripwise"
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";          // <-- your MySQL user
    private static final String PASS = "HAMZHAMZ123!"; // <-- your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // optional but fine
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found on classpath", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}