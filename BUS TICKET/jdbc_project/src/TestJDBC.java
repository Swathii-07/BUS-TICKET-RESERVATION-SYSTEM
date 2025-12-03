import java.sql.*;

public class TestJDBC {
    public static void main(String[] args) {
        try {
            // Load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ JDBC Driver Loaded Successfully!");

            // Connect to MySQL
            String url = "jdbc:mysql://localhost:3306/bus_reservation";
            String user = "root";        // your MySQL username
            String password = "root";    // your MySQL password

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to MySQL!");

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
