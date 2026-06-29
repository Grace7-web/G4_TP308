package cm.mobilemoney.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire de connexion JDBC à MySQL.
 */
public class ConnexionDB {

    private static final String URL      =
            "jdbc:mysql://localhost:3306/MobileMoney";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // ton mot de passe XAMPP

    public static Connection getConnexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Test rapide — à exécuter une seule fois pour vérifier */
    public static void testerConnexion() {
        try (Connection conn = getConnexion()) {
            System.out.println("✓ Connexion réussie : " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("✗ Échec : " + e.getMessage());
        }
    }
}