package cm.mobilemoney.service;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;

import java.util.List;

/**
 * Interface du service métier Mobile Money.
 *
 * Ce contrat définit toutes les opérations financières disponibles.
 * L'Équipe Persistance (DAO JDBC) doit implémenter {@code ICompteDAO}
 * et l'injecter dans l'implémentation concrète de cette interface.
 *
 * L'Équipe IHM Swing appelle UNIQUEMENT cette interface — jamais
 * directement le DAO. Cela garantit la séparation des couches.
 *
 * @author Équipe Core & Métier — Projet 4
 */
public interface ICompteService {

    // ── Gestion des comptes ───────────────────────────────────────────────────

    /**
     * Crée un nouveau compte et le persiste en base de données.
     *
     * @param titulaire    Nom du titulaire
     * @param soldeInitial Mise de départ (>= Compte.SOLDE_MINIMUM)
     * @return Le compte créé avec son numéro généré
     * @throws MobileMoneyException.MontantInvalideException si le solde initial est insuffisant
     */
    Compte creerCompte(String titulaire, double soldeInitial)
            throws MobileMoneyException;

    /**
     * Recherche un compte par son numéro.
     *
     * @param numero Numéro du compte
     * @return Le compte trouvé
     * @throws MobileMoneyException.CompteInexistantException si le compte n'existe pas
     */
    Compte rechercherCompte(String numero)
            throws MobileMoneyException;

    /**
     * Retourne la liste de tous les comptes actifs du système.
     *
     * @return Liste de comptes (peut être vide, jamais null)
     */
    List<Compte> listerTousLesComptes();

    /**
     * Bloque ou débloque un compte.
     *
     * @param numero Numéro du compte
     * @param actif  true pour activer, false pour bloquer
     * @throws MobileMoneyException.CompteInexistantException si le compte n'existe pas
     */
    void modifierStatutCompte(String numero, boolean actif)
            throws MobileMoneyException;

    // ── Opérations financières ────────────────────────────────────────────────

    /**
     * Dépose une somme d'argent sur un compte (crédit externe, sans commission).
     *
     * @param numeroCompte Numéro du compte destinataire
     * @param montant      Montant en FCFA (> 0, multiple de 500)
     * @return La transaction enregistrée
     * @throws MobileMoneyException si le compte est inexistant, inactif ou montant invalide
     */
    Transaction deposer(String numeroCompte, double montant)
            throws MobileMoneyException;

    /**
     * Retire une somme du compte (débit, avec prélèvement de commission).
     * La commission de retrait est de {@code CompteService.TAUX_COMMISSION_RETRAIT}%.
     *
     * @param numeroCompte Numéro du compte à débiter
     * @param montant      Montant souhaité en FCFA
     * @return La transaction enregistrée
     * @throws MobileMoneyException.SoldeInsuffisantException si montant + commission > solde
     * @throws MobileMoneyException.LimiteTransactionException si le plafond est dépassé
     */
    Transaction retirer(String numeroCompte, double montant)
            throws MobileMoneyException;

    /**
     * Transfère une somme d'un compte vers un autre.
     *
     * Règle atomique (garantie par la transaction SQL côté DAO) :
     * soit le débit ET le crédit réussissent ensemble, soit aucun n'est appliqué.
     *
     * La commission de transfert est de {@code CompteService.TAUX_COMMISSION_TRANSFERT}%.
     * Elle est prélevée sur le compte source EN PLUS du montant transféré.
     *
     * @param numeroSource      Numéro du compte émetteur
     * @param numeroDestination Numéro du compte récepteur
     * @param montant           Montant à transférer en FCFA
     * @return La transaction enregistrée
     * @throws MobileMoneyException si l'une des deux parties est invalide ou solde insuffisant
     */
    Transaction transferer(String numeroSource, String numeroDestination, double montant)
            throws MobileMoneyException;

    // ── Historique ────────────────────────────────────────────────────────────

    /**
     * Retourne l'historique complet des transactions d'un compte,
     * trié du plus récent au plus ancien.
     *
     * @param numeroCompte Numéro du compte
     * @return Liste de transactions triées (jamais null)
     * @throws MobileMoneyException.CompteInexistantException si le compte n'existe pas
     */
    List<Transaction> obtenirHistorique(String numeroCompte)
            throws MobileMoneyException;

    /**
     * Retourne toutes les transactions du système (pour l'administrateur).
     * Utilisé par le SwingWorker de l'Équipe IHM pour le chargement asynchrone.
     *
     * @return Liste complète triée par date décroissante
     */
    List<Transaction> obtenirToutesLesTransactions();
}
