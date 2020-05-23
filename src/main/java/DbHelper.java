import akka.actor.AbstractActor;
import message.UpdateDbRequest;
import java.sql.*;

public class DbHelper extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UpdateDbRequest.class, msg -> {
                    sender().tell(updateProduct(msg.product), self());
                })
                .build();
    }

    private Integer updateProduct(String product) throws SQLException {

        Connection conn = null;
        Statement statement = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:products.db");
            conn.setAutoCommit(true);
            statement = conn.createStatement();

            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM PRODUCT WHERE name = '" + product + "'");
            rs.next();
            int count = rs.getInt("COUNT(*)");

            String SQL = "";

            if (count == 0) {
                SQL = "INSERT INTO PRODUCT (NAME, QUESTIONS_NO) " +
                        "VALUES ('" + product + "', 1)";

                statement.executeUpdate(SQL);
            } else {

                SQL = "SELECT * FROM PRODUCT WHERE NAME='" + product + "'";
                rs = statement.executeQuery(SQL);
                int currNo = rs.getInt("QUESTIONS_NO");


                SQL = "UPDATE PRODUCT SET QUESTIONS_NO=" + (++currNo) + " WHERE NAME='" + product + "'";
                statement.executeUpdate(SQL);
            }

            SQL = "SELECT QUESTIONS_NO FROM PRODUCT WHERE NAME='" + product + "'";
            rs = statement.executeQuery(SQL);

            return rs.getInt("QUESTIONS_NO");

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        } finally {
            statement.close();
            conn.close();
        }

        return null;
    }


}
