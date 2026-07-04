package cm.mobilemoney.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Classe utilitaire de connexion JDBC à MySQL.
 */
public class ConnexionDB {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME  = "mobile_money";
    private static final String URL      = BASE_URL + DB_NAME;
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // ton mot de passe XAMPP

    public static Connection getConnexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Initialise la base de données et les tables si elles n'existent pas. */
    public static void initialiserBaseDeDonnees() {
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. Création de la base
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4;");
            stmt.executeUpdate("USE " + DB_NAME + ";");

            // 2. Création de la table comptes
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comptes (" +
                    "numero VARCHAR(20) PRIMARY KEY," +
                    "titulaire VARCHAR(100) NOT NULL," +
                    "solde DECIMAL(15,2) DEFAULT 0.00," +
                    "actif BOOLEAN DEFAULT TRUE," +
                    "mot_de_passe VARCHAR(64)," +
                    "question_secrete VARCHAR(200)," +
                    "reponse_secrete VARCHAR(64)" +
                    ");");

            // 3. Création de la table transactions
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "source VARCHAR(20)," +
                    "destination VARCHAR(20)," +
                    "montant DECIMAL(15,2) NOT NULL," +
                    "type VARCHAR(20) NOT NULL," +
                    "commission DECIMAL(15,2) DEFAULT 0.00," +
                    "succes BOOLEAN DEFAULT TRUE," +
                    "date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // 4. Insertion des données par défaut si la table est vide
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM comptes")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO comptes (numero, titulaire, solde, actif, mot_de_passe, question_secrete, reponse_secrete) VALUES " +
                            "('CM-001','GHADEUNE Grace',  500000.00,TRUE," +
                            "'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'," +
                            "'Ville de naissance ?'," +
                            "'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4')," +
                            "('CM-002','GWOS Christine',  320000.00,TRUE," +
                            "'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'," +
                            "'Ville de naissance ?'," +
                            "'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4')," +
                            "('CM-003','FOKOU Tedy',      150000.00,TRUE," +
                            "'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'," +
                            "'Ville de naissance ?'," +
                            "'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4');");
                }
            }

            System.out.println("✓ Base de données initialisée avec succès !");

        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de l'initialisation de la BDD : " + e.getMessage());
            System.err.println("Assurez-vous que votre serveur MySQL est démarré.");
        }
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