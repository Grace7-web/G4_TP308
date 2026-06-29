package cm.mobilemoney.service;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.exception.MobileMoneyException.*;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.metier.Transaction.TypeTransaction;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation du service métier Mobile Money.
 *
 * Responsabilités de cette classe :
 *  - Valider toutes les règles métier AVANT d'appeler le DAO
 *  - Calculer les commissions
 *  - Vérifier les plafonds réglementaires
 *  - Orchestrer les appels au DAO (couche persistance)
 *
 * Cette classe ne connaît PAS MySQL, ni JDBC, ni Swing.
 * Elle ne dépend que de l'interface {@code ICompteDAO}.
 *
 * INJECTION DE DÉPENDANCE : le DAO est passé au constructeur,
 * ce qui permet de le remplacer facilement (ex: mock pour les tests).
 *
 * @author Équipe Core & Métier — Projet 4
 */
public class CompteService implements ICompteService {

    // ── Constantes des règles métier ──────────────────────────────────────────

    /** Taux de commission sur les RETRAITS (en %) */
    public static final double TAUX_COMMISSION_RETRAIT   = 1.0;   // 1%

    /** Taux de commission sur les TRANSFERTS (en %) */
    public static final double TAUX_COMMISSION_TRANSFERT = 0.5;   // 0.5%

    /** Plafond maximum par opération de retrait (FCFA) */
    public static final double PLAFOND_RETRAIT           = 500_000.0;

    /** Plafond maximum par opération de transfert (FCFA) */
    public static final double PLAFOND_TRANSFERT         = 1_000_000.0;

    /** Montant minimum d'une transaction (FCFA) */
    public static final double MONTANT_MINIMUM           = 500.0;

    /** Le montant doit être un multiple de cette valeur (FCFA) */
    public static final double MULTIPLE_AUTORISE         = 500.0;

    // ── Dépendance injectée ───────────────────────────────────────────────────

    private final ICompteDAO compteDAO;

    // ── Constructeur avec injection de dépendance ─────────────────────────────

    /**
     * @param compteDAO Implémentation du DAO fournie par l'Équipe Persistance.
     *                  Ne doit pas être null.
     */
    public CompteService(ICompteDAO compteDAO) {
        if (compteDAO == null) {
            throw new IllegalArgumentException("Le DAO ne peut pas être null.");
        }
        this.compteDAO = compteDAO;
    }

    // ── Gestion des comptes ───────────────────────────────────────────────────

    @Override
    public Compte creerCompte(String titulaire, double soldeInitial)
            throws MobileMoneyException {

        // Validation du solde initial
        validerMontant(soldeInitial, "dépôt initial");

        // Génération du numéro de compte unique
        String numero = genererNumeroCompte();

        // Création de l'objet métier (le constructeur valide titulaire et solde)
        Compte nouveauCompte = new Compte(numero, titulaire, soldeInitial);

        // Persistance via le DAO
        compteDAO.insererCompte(nouveauCompte);

        System.out.printf("[SERVICE] Compte créé : %s%n", nouveauCompte);
        return nouveauCompte;
    }

    @Override
    public Compte rechercherCompte(String numero) throws MobileMoneyException {
        if (numero == null || numero.isBlank()) {
            throw new MontantInvalideException(0, "Le numéro de compte est vide.");
        }

        Compte compte = compteDAO.trouverParNumero(numero.trim().toUpperCase());

        if (compte == null) {
            throw new CompteInexistantException(numero);
        }

        return compte;
    }

    @Override
    public List<Compte> listerTousLesComptes() {
        try {
            return compteDAO.lireTous();
        } catch (MobileMoneyException e) {
            System.err.println("[SERVICE] Erreur lors de la lecture des comptes : " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void modifierStatutCompte(String numero, boolean actif)
            throws MobileMoneyException {

        // Vérifie que le compte existe
        rechercherCompte(numero);
        compteDAO.mettreAJourStatut(numero, actif);

        System.out.printf("[SERVICE] Compte %s : statut mis à jour → %s%n",
                numero, actif ? "ACTIF" : "BLOQUÉ");
    }

    // ── Opérations financières ────────────────────────────────────────────────

    @Override
    public Transaction deposer(String numeroCompte, double montant)
            throws MobileMoneyException {

        // 1. Validation du montant
        validerMontant(montant, "dépôt");

        // 2. Récupération et validation du compte
        Compte compte = rechercherCompte(numeroCompte);
        verifierCompteActif(compte);

        // 3. Application du crédit (pas de commission sur les dépôts)
        compte.crediter(montant);

        // 4. Mise à jour du solde en base
        compteDAO.mettreAJourSolde(compte.getNumero(), compte.getSolde());

        // 5. Enregistrement de la transaction
        Transaction transaction = new Transaction(
                TypeTransaction.DEPOT,
                null,               // source = externe
                compte.getNumero(),
                montant,
                0.0,                // pas de commission pour un dépôt
                true
        );
        compteDAO.enregistrerTransaction(transaction);

        System.out.printf("[SERVICE] DÉPÔT : %.2f FCFA → %s | Nouveau solde : %.2f FCFA%n",
                montant, compte.getNumero(), compte.getSolde());

        return transaction;
    }

    @Override
    public Transaction retirer(String numeroCompte, double montant)
            throws MobileMoneyException {

        // 1. Validation du montant
        validerMontant(montant, "retrait");

        // 2. Vérification du plafond réglementaire
        if (montant > PLAFOND_RETRAIT) {
            throw new LimiteTransactionException(montant, PLAFOND_RETRAIT, "retrait");
        }

        // 3. Récupération et validation du compte
        Compte compte = rechercherCompte(numeroCompte);
        verifierCompteActif(compte);

        // 4. Calcul de la commission
        double commission    = arrondir(montant * TAUX_COMMISSION_RETRAIT / 100.0);
        double montantTotal  = montant + commission;

        // 5. Vérification de la suffisance du solde
        // On vérifie que le solde restant ne descend pas sous le seuil minimum
        double soldeApresOperation = compte.getSolde() - montantTotal;
        if (soldeApresOperation < Compte.SOLDE_MINIMUM) {
            throw new SoldeInsuffisantException(
                    compte.getSolde(),
                    montantTotal + Compte.SOLDE_MINIMUM   // ce qu'il faudrait avoir
            );
        }

        // 6. Application du débit
        compte.debiter(montantTotal);

        // 7. Mise à jour en base
        compteDAO.mettreAJourSolde(compte.getNumero(), compte.getSolde());

        // 8. Enregistrement de la transaction
        Transaction transaction = new Transaction(
                TypeTransaction.RETRAIT,
                compte.getNumero(),
                null,               // destination = externe
                montant,
                commission,
                true
        );
        compteDAO.enregistrerTransaction(transaction);

        System.out.printf("[SERVICE] RETRAIT : %.2f FCFA (commission %.2f) ← %s | Solde restant : %.2f FCFA%n",
                montant, commission, compte.getNumero(), compte.getSolde());

        return transaction;
    }

    @Override
    public Transaction transferer(String numeroSource,
                                  String numeroDestination,
                                  double montant)
            throws MobileMoneyException {

        // 1. Validation : les deux numéros doivent être différents
        if (numeroSource == null || numeroDestination == null) {
            throw new CompteInexistantException("null");
        }
        if (numeroSource.trim().equalsIgnoreCase(numeroDestination.trim())) {
            throw new MontantInvalideException(montant,
                    "Impossible de transférer vers le même compte.");
        }

        // 2. Validation du montant
        validerMontant(montant, "transfert");

        // 3. Vérification du plafond
        if (montant > PLAFOND_TRANSFERT) {
            throw new LimiteTransactionException(montant, PLAFOND_TRANSFERT, "transfert");
        }

        // 4. Validation des deux comptes
        Compte source      = rechercherCompte(numeroSource);
        Compte destination = rechercherCompte(numeroDestination);
        verifierCompteActif(source);
        verifierCompteActif(destination);

        // 5. Calcul de la commission (prélevée sur la source EN PLUS du montant)
        double commission   = arrondir(montant * TAUX_COMMISSION_TRANSFERT / 100.0);
        double totalPrelevement = montant + commission;

        // 6. Vérification du solde source
        double soldeApres = source.getSolde() - totalPrelevement;
        if (soldeApres < Compte.SOLDE_MINIMUM) {
            throw new SoldeInsuffisantException(
                    source.getSolde(),
                    totalPrelevement + Compte.SOLDE_MINIMUM
            );
        }

        // 7. Délégation au DAO : opération atomique (transaction SQL)
        // C'est le DAO qui garantit l'atomicité avec setAutoCommit(false)
        Transaction transaction = compteDAO.executerTransfert(
                source.getNumero(),
                destination.getNumero(),
                montant,
                commission
        );

        System.out.printf("[SERVICE] TRANSFERT : %.2f FCFA (commission %.2f) | %s → %s%n",
                montant, commission, source.getNumero(), destination.getNumero());

        return transaction;
    }

    // ── Historique ────────────────────────────────────────────────────────────

    @Override
    public List<Transaction> obtenirHistorique(String numeroCompte)
            throws MobileMoneyException {

        // Vérifie que le compte existe
        rechercherCompte(numeroCompte);

        List<Transaction> historique = compteDAO.lireTransactionsParCompte(numeroCompte);
        Collections.sort(historique);   // tri : du plus récent au plus ancien

        return historique;
    }

    @Override
    public List<Transaction> obtenirToutesLesTransactions() {
        try {
            List<Transaction> toutes = compteDAO.lireToutesLesTransactions();
            Collections.sort(toutes);
            return toutes;
        } catch (MobileMoneyException e) {
            System.err.println("[SERVICE] Erreur chargement transactions : " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Méthodes privées utilitaires ──────────────────────────────────────────

    /**
     * Valide qu'un montant respecte toutes les règles métier.
     *
     * @throws MontantInvalideException si le montant est invalide
     */
    private void validerMontant(double montant, String typeOperation)
            throws MontantInvalideException {

        if (montant <= 0) {
            throw new MontantInvalideException(montant,
                    "Le montant d'un " + typeOperation + " doit être strictement positif.");
        }
        if (montant < MONTANT_MINIMUM) {
            throw new MontantInvalideException(montant,
                    "Le montant minimum est de " + MONTANT_MINIMUM + " FCFA.");
        }
        // Vérifie que le montant est un multiple de 500 FCFA
        if (montant % MULTIPLE_AUTORISE != 0) {
            throw new MontantInvalideException(montant,
                    "Le montant doit être un multiple de " + (int) MULTIPLE_AUTORISE + " FCFA.");
        }
    }

    /**
     * Vérifie qu'un compte est actif, sinon lève une exception.
     *
     * @throws CompteInactifException si le compte est bloqué
     */
    private void verifierCompteActif(Compte compte) throws CompteInactifException {
        if (!compte.isActif()) {
            throw new CompteInactifException(compte.getNumero());
        }
    }

    /**
     * Génère un numéro de compte unique au format "CM-XXXXXXXX".
     */
    private String genererNumeroCompte() {
        return "CM-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    /**
     * Arrondit un montant à 2 décimales (pour éviter les erreurs flottantes).
     */
    private double arrondir(double montant) {
        return Math.round(montant * 100.0) / 100.0;
    }
}
