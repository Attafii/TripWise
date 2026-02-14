package ui.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private static Connection conn;

    public static synchronized Connection get() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection("jdbc:h2:file:./tripwise_db;AUTO_SERVER=TRUE");
                try (Statement st = conn.createStatement()) {
                    st.execute("RUNSCRIPT FROM 'classpath:db/mock.sql'");
                }
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
