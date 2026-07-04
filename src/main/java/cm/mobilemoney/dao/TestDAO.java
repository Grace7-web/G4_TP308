package cm.mobilemoney.dao;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.service.CompteService;

public class TestDAO {

    public static void main(String[] args) {

        // ── TEST 1 : Connexion à MySQL
        System.out.println("=== TEST 1 : CONNEXION ===");
        ConnexionDB.testerConnexion();

        // ── Créer le DAO et le Service
        CompteDAO dao = new CompteDAO();
        CompteService service = new CompteService(dao);

        // ── TEST 2 : Lire tous les comptes
        System.out.println("\n=== TEST 2 : LECTURE COMPTES ===");
        try {
            for (Compte c : dao.lireTous()) {
                System.out.println(c);
            }
        } catch (MobileMoneyException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        // ── TEST 3 : Chercher un compte
        System.out.println("\n=== TEST 3 : CHERCHER UN COMPTE ===");
        try {
            Compte c = service.rechercherCompte("CM-001");
            System.out.println("Trouvé : " + c);
        } catch (MobileMoneyException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        // ── TEST 4 : Dépôt
        System.out.println("\n=== TEST 4 : DÉPÔT ===");
        try {
            Transaction t = service.deposer("CM-001", 10000.0);
            System.out.println("Dépôt effectué : " + t);
        } catch (MobileMoneyException e) {
            System.err.println("Erreur dépôt : " + e.getMessage());
        }

        // ── TEST 5 : Retrait
        System.out.println("\n=== TEST 5 : RETRAIT ===");
        try {
            Transaction t = service.retirer("CM-002", 5000.0);
            System.out.println("Retrait effectué : " + t);
        } catch (MobileMoneyException e) {
            System.err.println("Erreur retrait : " + e.getMessage());
        }

        // ── TEST 6 : Transfert (le plus important — rollback)
        System.out.println("\n=== TEST 6 : TRANSFERT ===");
        try {
            Transaction t = service.transferer("CM-001", "CM-002", 20000.0);
            System.out.println("Transfert effectué : " + t);
        } catch (MobileMoneyException e) {
            System.err.println("Erreur transfert : " + e.getMessage());
        }

        // TEST 7 : ROLLBACK (solde insuffisant)
        // CM-004 a 80000 FCFA — on essaie de transférer 500000 FCFA
        // → solde insuffisant → rollback SQL déclenché
        System.out.println("\n=== TEST 7 : ROLLBACK RÉEL (solde insuffisant) ===");
        try {
            service.transferer("CM-004", "CM-002", 500000.0);
            System.out.println("❌ PROBLÈME : le transfert aurait dû échouer !");
        } catch (MobileMoneyException e) {
            System.out.println("✓ Rollback déclenché : " + e.getMessage());
        }

        // Vérifier que le solde de CM-004 n'a PAS changé
        System.out.println("\n=== VÉRIFICATION SOLDE APRÈS ROLLBACK ===");
        try {
            Compte c = service.rechercherCompte("CM-004");
            System.out.println("Solde CM-004 après rollback : "
                    + c.getSolde() + " FCFA");
            System.out.println("(doit rester à 80000 FCFA — rien n'a changé)");
        } catch (MobileMoneyException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        // ── TEST 8 : Historique transactions
        System.out.println("\n=== TEST 8 : HISTORIQUE ===");
        try {
            for (Transaction t : service.obtenirHistorique("CM-001")) {
                System.out.println(t);
            }
        } catch (MobileMoneyException e) {
            System.err.println("Erreur historique : " + e.getMessage());
        }

        System.out.println("\n=== TOUS LES TESTS TERMINÉS ===");
    }
}