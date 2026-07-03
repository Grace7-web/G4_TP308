package cm.mobilemoney.exception;

/**
 * Hiérarchie d'exceptions personnalisées pour le système Mobile Money.
 *
 * Architecture :
 *   MobileMoneyException (racine — checked)
 *     ├── SoldeInsuffisantException
 *     ├── CompteInexistantException
 *     ├── CompteInactifException
 *     ├── MontantInvalideException
 *     ├── LimiteTransactionException
 *     └── AuthentificationException   (mot de passe / réponse secrète invalide)
 *
 * Toutes sont des checked exceptions (extends Exception) pour forcer
 * l'équipe DAO et l'équipe IHM à les capturer explicitement.
 *
 * @author Équipe Core & Métier — Projet 4
 */

public class MobileMoneyException extends Exception {

    private static final long serialVersionUID = 1L;

    public MobileMoneyException(String message) {
        super(message);
    }

    public MobileMoneyException(String message, Throwable cause) {
        super(message, cause);
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. Solde insuffisant
    // ═══════════════════════════════════════════════════════════════

    /**
     * Levée lorsque le solde du compte source est insuffisant pour
     * couvrir le montant + la commission d'une opération.
     */
    public static class SoldeInsuffisantException extends MobileMoneyException {

        private static final long serialVersionUID = 1L;

        private final double soldeActuel;
        private final double montantRequis;

        public SoldeInsuffisantException(double soldeActuel, double montantRequis) {
            super(String.format(
                    "Solde insuffisant. Solde actuel : %.2f FCFA. "
                            + "Montant requis (avec commission) : %.2f FCFA. "
                            + "Manque : %.2f FCFA.",
                    soldeActuel, montantRequis, (montantRequis - soldeActuel)
            ));
            this.soldeActuel   = soldeActuel;
            this.montantRequis = montantRequis;
        }

        public double getSoldeActuel()   { return soldeActuel; }
        public double getMontantRequis() { return montantRequis; }
        public double getDeficit()       { return montantRequis - soldeActuel; }
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. Compte inexistant
    // ═══════════════════════════════════════════════════════════════

    /**
     * Levée lorsqu'un numéro de compte ne correspond à aucun enregistrement
     * dans la base de données.
     */
    public static class CompteInexistantException extends MobileMoneyException {

        private static final long serialVersionUID = 1L;

        private final String numeroCompte;

        public CompteInexistantException(String numeroCompte) {
            super("Compte introuvable : aucun compte avec le numéro « " + numeroCompte + " » n'existe dans le système.");
            this.numeroCompte = numeroCompte;
        }

        public String getNumeroCompte() { return numeroCompte; }
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. Compte inactif / bloqué
    // ═══════════════════════════════════════════════════════════════

    /**
     * Levée lorsqu'une opération est tentée sur un compte désactivé ou bloqué
     * (suite à fraude, demande client, etc.).
     */
    public static class CompteInactifException extends MobileMoneyException {

        private static final long serialVersionUID = 1L;

        private final String numeroCompte;

        public CompteInactifException(String numeroCompte) {
            super("Opération impossible : le compte « " + numeroCompte
                    + " » est inactif ou bloqué. Contactez votre agence.");
            this.numeroCompte = numeroCompte;
        }

        public String getNumeroCompte() { return numeroCompte; }
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. Montant invalide
    // ═══════════════════════════════════════════════════════════════

    /**
     * Levée lorsque le montant saisi ne respecte pas les contraintes métier :
     * négatif, nul, non-multiple de 500 FCFA, ou supérieur au plafond journalier.
     */
    public static class MontantInvalideException extends MobileMoneyException {

        private static final long serialVersionUID = 1L;

        private final double montantSaisi;

        public MontantInvalideException(double montantSaisi, String raison) {
            super(String.format(
                    "Montant invalide (%.2f FCFA) : %s", montantSaisi, raison
            ));
            this.montantSaisi = montantSaisi;
        }

        public double getMontantSaisi() { return montantSaisi; }
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. Limite de transaction dépassée
    // ═══════════════════════════════════════════════════════════════

    /**
     * Levée lorsque le montant d'une opération dépasse les plafonds
     * réglementaires fixés (ex: 500 000 FCFA par transfert, 1 000 000/jour).
     */
    public static class LimiteTransactionException extends MobileMoneyException {

        private static final long serialVersionUID = 1L;

        private final double montantDemande;
        private final double plafondAutorise;

        public LimiteTransactionException(double montantDemande, double plafondAutorise, String typeOperation) {
            super(String.format(
                    "Plafond de %s dépassé. Montant demandé : %.2f FCFA. "
                            + "Plafond autorisé : %.2f FCFA.",
                    typeOperation, montantDemande, plafondAutorise
            ));
            this.montantDemande   = montantDemande;
            this.plafondAutorise  = plafondAutorise;
        }

        public double getMontantDemande()  { return montantDemande; }
        public double getPlafondAutorise() { return plafondAutorise; }
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. Authentification échouée (mot de passe / réponse secrète)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Levée lorsque le mot de passe saisi est incorrect, qu'aucune question
     * secrète n'est définie pour le compte, ou que la réponse secrète
     * fournie lors de la récupération est incorrecte.
     */
    public static class AuthentificationException extends MobileMoneyException {

        private static final long serialVersionUID = 1L;

        public AuthentificationException(String message) {
            super(message);
        }
    }
}
