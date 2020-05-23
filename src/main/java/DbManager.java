import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbManager {
    public static void main( String args[] ) {
        Connection conn = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:products.db");

            Statement statement = conn.createStatement();

            String SQL = "CREATE TABLE PRODUCT " +
                    "(NAME TEXT PRIMARY KEY NOT NULL, " +
                    " QUESTIONS_NO INT NOT NULL)";

            statement.execute(SQL);
            statement.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
}
