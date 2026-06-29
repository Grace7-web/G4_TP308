package cm.mobilemoney.service;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;

import java.util.List;

/**
 * Interface DAO (Data Access Object) — contrat de persistance pour l'Équipe 2.
 *
 * Cette interface définit EXACTEMENT ce que l'Équipe Persistance doit implémenter
 * en JDBC/MySQL. Le service métier ({@code CompteService}) utilise UNIQUEMENT
 * cette interface, jamais une classe concrète.
 *
 * DIRECTIVES POUR L'ÉQUIPE PERSISTANCE :
 * - Créer une classe {@code CompteDAO} dans le package {@code cm.mobilemoney.dao}
 *   qui implémente cette interface.
 * - Toutes les méthodes doivent utiliser des {@code PreparedStatement}.
 * - La méthode {@code executerTransfert()} DOIT utiliser une transaction SQL
 *   avec {@code conn.setAutoCommit(false)} + rollback en cas d'erreur.
 * - Fermer systématiquement les flux (try-with-resources).
 *
 * @author Équipe Core & Métier — Projet 4
 */
public interface ICompteDAO {

    // ── CRUD Comptes ──────────────────────────────────────────────────────────

    /**
     * Insère un nouveau compte en base de données.
     * SQL cible : INSERT INTO comptes (numero, titulaire, solde, date_creation, actif) VALUES (?,?,?,?,?)
     */
    void insererCompte(Compte compte) throws MobileMoneyException;

    /**
     * Recherche un compte par son numéro unique.
     * SQL cible : SELECT * FROM comptes WHERE numero = ?
     *
     * @return null si aucun compte trouvé (le service convertira en exception)
     */
    Compte trouverParNumero(String numero) throws MobileMoneyException;

    /**
     * Retourne la liste de tous les comptes.
     * SQL cible : SELECT * FROM comptes ORDER BY titulaire
     */
    List<Compte> lireTous() throws MobileMoneyException;

    /**
     * Met à jour le solde d'un compte après une opération.
     * SQL cible : UPDATE comptes SET solde = ? WHERE numero = ?
     */
    void mettreAJourSolde(String numero, double nouveauSolde) throws MobileMoneyException;

    /**
     * Active ou bloque un compte.
     * SQL cible : UPDATE comptes SET actif = ? WHERE numero = ?
     */
    void mettreAJourStatut(String numero, boolean actif) throws MobileMoneyException;

    // ── Transactions ──────────────────────────────────────────────────────────

    /**
     * Enregistre une transaction simple (dépôt ou retrait) en base.
     * SQL cible : INSERT INTO transactions (id, source, destination, montant, commission, date_heure, type, succes)
     */
    void enregistrerTransaction(Transaction transaction) throws MobileMoneyException;

    /**
     * Exécute un transfert atomique entre deux comptes.
     *
     * OBLIGATOIREMENT dans une transaction SQL :
     * <pre>
     *   conn.setAutoCommit(false);
     *   try {
     *       // UPDATE comptes SET solde = solde - (montant + commission) WHERE numero = numeroSource
     *       // UPDATE comptes SET solde = solde + montant WHERE numero = numeroDestination
     *       // INSERT INTO transactions ...
     *       conn.commit();
     *   } catch (SQLException e) {
     *       conn.rollback();
     *       throw new MobileMoneyException(..., e);
     *   } finally {
     *       conn.setAutoCommit(true);
     *   }
     * </pre>
     *
     * @param numeroSource      Compte à débiter
     * @param numeroDestination Compte à créditer
     * @param montant           Montant transféré
     * @param commission        Commission prélevée sur la source
     * @return La transaction enregistrée
     */
    Transaction executerTransfert(String numeroSource,
                                  String numeroDestination,
                                  double montant,
                                  double commission) throws MobileMoneyException;

    /**
     * Retourne l'historique des transactions d'un compte (source ou destination).
     * SQL cible : SELECT * FROM transactions WHERE source = ? OR destination = ? ORDER BY date_heure DESC
     */
    List<Transaction> lireTransactionsParCompte(String numeroCompte) throws MobileMoneyException;

    /**
     * Retourne toutes les transactions du système, triées par date décroissante.
     * SQL cible : SELECT * FROM transactions ORDER BY date_heure DESC
     */
    List<Transaction> lireToutesLesTransactions() throws MobileMoneyException;
}
