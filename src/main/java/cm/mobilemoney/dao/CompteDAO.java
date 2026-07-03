package cm.mobilemoney.dao;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.metier.Transaction.TypeTransaction;
import cm.mobilemoney.service.ICompteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de ICompteDAO.
 * Toutes les opérations utilisent PreparedStatement (Semaine 8).
 * Le transfert utilise setAutoCommit(false) + rollback (Semaine 5).
 */
public class CompteDAO implements ICompteDAO {

    // ─────────────────────────────────────────────────────────────
    // MÉTHODE PRIVÉE — mapper une ligne ResultSet → objet Compte
    // Évite de répéter le même code dans chaque méthode
    // ─────────────────────────────────────────────────────────────
    private Compte mapperCompte(ResultSet rs) throws SQLException {
        Compte compte = new Compte(
                rs.getString("numero"),
                rs.getString("titulaire"),
                rs.getDouble("solde"),
                rs.getString("mot_de_passe"),
                rs.getString("question_secrete"),
                rs.getString("reponse_secrete")
        );
        compte.setActif(rs.getBoolean("actif"));
        return compte;
    }

    // ─────────────────────────────────────────────────────────────
    // INSÉRER un nouveau compte en base
    // ─────────────────────────────────────────────────────────────
    @Override
    public void insererCompte(Compte compte) throws MobileMoneyException {
        String sql = "INSERT INTO comptes " +
                "(numero, titulaire, solde, mot_de_passe, " +
                "question_secrete, reponse_secrete) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, compte.getNumero());
            ps.setString(2, compte.getTitulaire());
            ps.setDouble(3, compte.getSolde());
            ps.setString(4, compte.getMotDePasseHash());
            ps.setString(5, compte.getQuestionSecrete());
            ps.setString(6, compte.getReponseSecreteHash());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur insertion compte : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TROUVER un compte par son numéro
    // ─────────────────────────────────────────────────────────────
    @Override
    public Compte trouverParNumero(String numero)
            throws MobileMoneyException {
        String sql = "SELECT * FROM comptes WHERE numero = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperCompte(rs);
                }
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur recherche compte : " + e.getMessage(), e);
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────
    // LIRE tous les comptes
    // ─────────────────────────────────────────────────────────────
    @Override
    public List<Compte> lireTous() throws MobileMoneyException {
        List<Compte> liste = new ArrayList<>();
        String sql = "SELECT * FROM comptes ORDER BY titulaire ASC";

        try (Connection conn = ConnexionDB.getConnexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(mapperCompte(rs));
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur lecture comptes : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────
    // METTRE À JOUR le solde d'un compte
    // ─────────────────────────────────────────────────────────────
    @Override
    public void mettreAJourSolde(String numero, double nouveauSolde)
            throws MobileMoneyException {
        String sql = "UPDATE comptes SET solde = ? WHERE numero = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, nouveauSolde);
            ps.setString(2, numero);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur mise à jour solde : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // METTRE À JOUR le statut actif/inactif d'un compte
    // ─────────────────────────────────────────────────────────────
    @Override
    public void mettreAJourStatut(String numero, boolean actif)
            throws MobileMoneyException {
        String sql = "UPDATE comptes SET actif = ? WHERE numero = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, actif);
            ps.setString(2, numero);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur mise à jour statut : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
// METTRE À JOUR le mot de passe d'un compte
// ─────────────────────────────────────────────────────────────
    @Override
    public void mettreAJourMotDePasse(String numero, String motDePasseHash)
            throws MobileMoneyException {
        String sql = "UPDATE comptes SET mot_de_passe = ? WHERE numero = ?";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, motDePasseHash);
            ps.setString(2, numero);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur mise à jour mot de passe : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ENREGISTRER une transaction en base
    // ─────────────────────────────────────────────────────────────
    @Override
    public void enregistrerTransaction(Transaction t)
            throws MobileMoneyException {
        String sql = "INSERT INTO transactions " +
                "(source, destination, montant, type, commission, succes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getNumeroSource());
            ps.setString(2, t.getNumeroDestination());
            ps.setDouble(3, t.getMontant());
            ps.setString(4, t.getType().name());
            ps.setDouble(5, t.getCommission());
            ps.setBoolean(6, t.isSucces());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur enregistrement transaction : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EXÉCUTER un transfert atomique
    // C'est la méthode la plus importante —
    // setAutoCommit(false) garantit que les 3 opérations
    // s'exécutent TOUTES ou PAS DU TOUT (rollback si erreur)
    // ─────────────────────────────────────────────────────────────
    @Override
    public Transaction executerTransfert(String numeroSource,
                                         String numeroDest,
                                         double montant,
                                         double commission)
            throws MobileMoneyException {

        double totalDebit = montant + commission;

        try (Connection conn = ConnexionDB.getConnexion()) {

            conn.setAutoCommit(false); // début transaction SQL

            try {
                // 1. Débiter le compte source
                String sqlDebit =
                        "UPDATE comptes SET solde = solde - ? " +
                                "WHERE numero = ?";
                try (PreparedStatement ps =
                             conn.prepareStatement(sqlDebit)) {
                    ps.setDouble(1, totalDebit);
                    ps.setString(2, numeroSource);
                    ps.executeUpdate();
                }

                // 2. Créditer le compte destination
                String sqlCredit =
                        "UPDATE comptes SET solde = solde + ? " +
                                "WHERE numero = ?";
                try (PreparedStatement ps =
                             conn.prepareStatement(sqlCredit)) {
                    ps.setDouble(1, montant);
                    ps.setString(2, numeroDest);
                    ps.executeUpdate();
                }

                // 3. Enregistrer dans la table transactions
                String sqlTrans =
                        "INSERT INTO transactions " +
                                "(source, destination, montant, type, " +
                                "commission, succes) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps =
                             conn.prepareStatement(sqlTrans)) {
                    ps.setString(1, numeroSource);
                    ps.setString(2, numeroDest);
                    ps.setDouble(3, montant);
                    ps.setString(4, TypeTransaction.TRANSFERT.name());
                    ps.setDouble(5, commission);
                    ps.setBoolean(6, true);
                    ps.executeUpdate();
                }

                conn.commit(); // ✅ tout réussi
                conn.setAutoCommit(true);

                return new Transaction(
                        TypeTransaction.TRANSFERT,
                        numeroSource, numeroDest,
                        montant, commission, true);

            } catch (SQLException e) {
                conn.rollback(); // ❌ annule tout
                conn.setAutoCommit(true);
                throw new MobileMoneyException(
                        "Transfert annulé (rollback) : " +
                                e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur connexion transfert : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LIRE les transactions d'un compte précis
    // ─────────────────────────────────────────────────────────────
    @Override
    public List<Transaction> lireTransactionsParCompte(String numero)
            throws MobileMoneyException {
        List<Transaction> liste = new ArrayList<>();
        String sql =
                "SELECT * FROM transactions " +
                        "WHERE source = ? OR destination = ? " +
                        "ORDER BY date DESC";

        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            ps.setString(2, numero);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur lecture historique : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────
    // LIRE toutes les transactions du système
    // Utilisé par le SwingWorker de l'Équipe 4
    // ─────────────────────────────────────────────────────────────
    @Override
    public List<Transaction> lireToutesLesTransactions()
            throws MobileMoneyException {
        List<Transaction> liste = new ArrayList<>();
        String sql =
                "SELECT * FROM transactions ORDER BY date DESC";

        try (Connection conn = ConnexionDB.getConnexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(mapperTransaction(rs));
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur lecture transactions : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTHODE PRIVÉE — mapper une ligne ResultSet → objet Transaction
    // ─────────────────────────────────────────────────────────────
    private Transaction mapperTransaction(ResultSet rs)
            throws SQLException {
        return new Transaction(
                TypeTransaction.valueOf(rs.getString("type")),
                rs.getString("source"),
                rs.getString("destination"),
                rs.getDouble("montant"),
                rs.getDouble("commission"),
                rs.getBoolean("succes")
        );
    }
}