import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

class MySqlHelper {

    static Connection connection = null;
    static PreparedStatement insertAppInstlStatement = null;
    static PreparedStatement insertUserInfoStatement = null;


    MySqlHelper() throws SQLException {
        String myUrl = "jdbc:mysql://URL";
        Properties props = new Properties();
        props.setProperty("username","XX");
        props.setProperty("password", "XXX");
        props.setProperty("useLegacyDatetimeCode", "false");
        props.setProperty("serverTimezone", "UTC");
        connection = DriverManager.getConnection(myUrl, props);
    }

    static void ConnectToMySql(JSONObject jsonMessage) {
        try {
            System.out.println("Conneced to MySql");

            //build query to insert user info
            insertUserInfoStatement = insertUserInfo(jsonMessage, connection);
            insertUserInfoStatement.execute();

            //build query to insert installed apps
            String installedApps = String.valueOf(jsonMessage.get("installed_app"));
            installedApps  = installedApps.replaceAll("\\[|\\]", "");
            for (String app: installedApps.split(",")) {
                insertAppInstlStatement = insertInstalledApps(jsonMessage, connection, app);
                insertAppInstlStatement.execute();
            }

        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                if(insertUserInfoStatement != null || insertAppInstlStatement != null ||connection != null) {
                    insertUserInfoStatement.close();
                    insertAppInstlStatement.close();
                    connection.close();
                }
            } catch(SQLException se) {

            }
        }
    }

    private static PreparedStatement insertUserInfo(JSONObject jsonMessage, Connection connection) throws SQLException, JSONException {

        System.out.println("insertUserInfo");
        String insertUserInfoQueury = "INSERT INTO usersInfo (tuid, google_id, name, sex, age) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement insertStatement = connection.prepareStatement(insertUserInfoQueury);
        insertStatement.setString(1, String.valueOf(jsonMessage.get("tuid")));
        insertStatement.setString(2, String.valueOf(jsonMessage.get("google_id")));
        insertStatement.setString(3, String.valueOf(jsonMessage.get("name")));
        insertStatement.setString(4, String.valueOf(jsonMessage.get("sex")));
        insertStatement.setString(5, String.valueOf(jsonMessage.get("age")));

        return insertStatement;
    }

    private static PreparedStatement insertInstalledApps(JSONObject jsonMessage, Connection connection, String app) throws SQLException, JSONException {
        System.out.println("insertInstalledApps");

        String insertAppsQueury = "INSERT INTO usersApps (tuid, installed_app) VALUES (?, ?)";
        PreparedStatement insertStatement = connection.prepareStatement(insertAppsQueury);
        insertStatement.setString(1,  String.valueOf(jsonMessage.get("tuid")));
        insertStatement.setString(2,  app);

        return insertStatement;
    }
}
