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
 *
 * @author Équipe Persistance & Données — Projet 4
 */
public class CompteDAO implements ICompteDAO {

    // ──────────────────────────────────────────
    // CRUD COMPTES
    // ──────────────────────────────────────────

    @Override
    public void insererCompte(Compte compte) throws MobileMoneyException {
        String sql = "INSERT INTO comptes (numero, titulaire, solde) " +
                "VALUES (?, ?, ?)";
        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, compte.getNumero());
            ps.setString(2, compte.getTitulaire());
            ps.setDouble(3, compte.getSolde());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur insertion compte : " + e.getMessage(), e);
        }
    }

    @Override
    public Compte trouverParNumero(String numero) throws MobileMoneyException {
        String sql = "SELECT * FROM comptes WHERE numero = ?";
        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Compte(
                            rs.getString("numero"),
                            rs.getString("titulaire"),
                            rs.getDouble("solde")
                    );
                }
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur recherche compte : " + e.getMessage(), e);
        }
        return null; // le service convertira en CompteInexistantException
    }

    @Override
    public List<Compte> lireTous() throws MobileMoneyException {
        List<Compte> liste = new ArrayList<>();
        String sql = "SELECT * FROM comptes ORDER BY titulaire ASC";
        try (Connection conn = ConnexionDB.getConnexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Compte(
                        rs.getString("numero"),
                        rs.getString("titulaire"),
                        rs.getDouble("solde")
                ));
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur lecture comptes : " + e.getMessage(), e);
        }
        return liste;
    }

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

    // ──────────────────────────────────────────
    // TRANSACTIONS
    // ──────────────────────────────────────────

    @Override
    public void enregistrerTransaction(Transaction t)
            throws MobileMoneyException {
        String sql = "INSERT INTO transactions " +
                "(source, destination, montant, type) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getNumeroSource());
            ps.setString(2, t.getNumeroDestination());
            ps.setDouble(3, t.getMontant());
            ps.setString(4, t.getType().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur enregistrement transaction : " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction executerTransfert(String numeroSource,
                                         String numeroDestination,
                                         double montant,
                                         double commission)
            throws MobileMoneyException {

        // Semaine 5 + 8 : transaction SQL atomique
        // setAutoCommit(false) → commit() ou rollback()
        try (Connection conn = ConnexionDB.getConnexion()) {
            conn.setAutoCommit(false);
            try {
                double totalDebit = montant + commission;

                // 1. Débiter le compte source
                String sqlDebit = "UPDATE comptes SET solde = solde - ? " +
                        "WHERE numero = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDebit)) {
                    ps.setDouble(1, totalDebit);
                    ps.setString(2, numeroSource);
                    ps.executeUpdate();
                }

                // 2. Créditer le compte destination
                String sqlCredit = "UPDATE comptes SET solde = solde + ? " +
                        "WHERE numero = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlCredit)) {
                    ps.setDouble(1, montant);
                    ps.setString(2, numeroDestination);
                    ps.executeUpdate();
                }

                // 3. Enregistrer la transaction
                String sqlTrans = "INSERT INTO transactions " +
                        "(source, destination, montant, type) " +
                        "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlTrans)) {
                    ps.setString(1, numeroSource);
                    ps.setString(2, numeroDestination);
                    ps.setDouble(3, montant);
                    ps.setString(4, TypeTransaction.TRANSFERT.name());
                    ps.executeUpdate();
                }

                conn.commit(); // ✅ Tout réussi
                conn.setAutoCommit(true);

                return new Transaction(TypeTransaction.TRANSFERT,
                        numeroSource, numeroDestination, montant, commission, true);

            } catch (SQLException e) {
                conn.rollback(); // ❌ Annule tout
                conn.setAutoCommit(true);
                throw new MobileMoneyException(
                        "Transfert annulé (rollback) : " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur connexion transfert : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> lireTransactionsParCompte(String numeroCompte)
            throws MobileMoneyException {
        List<Transaction> liste = new ArrayList<>();
        String sql = "SELECT * FROM transactions " +
                "WHERE source = ? OR destination = ? " +
                "ORDER BY date DESC";
        try (Connection conn = ConnexionDB.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numeroCompte);
            ps.setString(2, numeroCompte);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Transaction(
                            TypeTransaction.valueOf(rs.getString("type")),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getDouble("montant"),
                            0.0,
                            true
                    ));
                }
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur lecture historique : " + e.getMessage(), e);
        }
        return liste;
    }

    @Override
    public List<Transaction> lireToutesLesTransactions()
            throws MobileMoneyException {
        List<Transaction> liste = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        try (Connection conn = ConnexionDB.getConnexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Transaction(
                        TypeTransaction.valueOf(rs.getString("type")),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getDouble("montant"),
                        0.0,
                        true
                ));
            }
        } catch (SQLException e) {
            throw new MobileMoneyException(
                    "Erreur lecture toutes transactions : " + e.getMessage(), e);
        }
        return liste;
    }
}