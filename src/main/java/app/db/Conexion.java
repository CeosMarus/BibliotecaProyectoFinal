package app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    /*private static final String URL  = "jdbc:sqlserver://localhost:1433;databaseName=SGIB;encrypt=false";
    private static final String USER = "sa";           // tu usuario
    private static final String PASS = "Dev2025!";  // tu contraseña*/
    //private static final String PASS = "V!V!EQAq5D6G";  // contraseña de mi docker (wilson)*/

    //Credenciales DB en linea
    private static final String URL  = "jdbc:sqlserver://rds11g.isbelasoft.com:1433;databaseName=p2g1b;encrypt=false";
    private static final String USER = "p2g1b";           // tu usuario
    private static final String PASS = "Umg@123";  // tu contraseña*/


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    //Método de prueba
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión exitosa a SQL Server");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
