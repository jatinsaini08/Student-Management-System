import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        // Apne MySQL ka URL, username aur password yahan daalein
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/studentdb",
                "root",
                "Poornima@2008" // <-- Yahan apna actual password likhein
        );
        return con;
    }
}