package cm.mobilemoney.service;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.metier.Transaction.TypeTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation fictive (Mock) du DAO pour les tests unitaires.
 * Simule une base de données en mémoire avec des HashMap.
 *
 * NE PAS utiliser en production — uniquement pour valider la logique métier
 * en attendant que l'Équipe Persistance livre le vrai CompteDAO JDBC.
 *
 * @author Équipe Core & Métier — Projet 4 (usage test uniquement)
 */
public class CompteDAOMock implements ICompteDAO {

    // Simulation de la table "comptes"
    private final Map<String, Compte> tableComptes = new HashMap<>();

    // Simulation de la table "transactions"
    private final List<Transaction> tableTransactions = new ArrayList<>();

    @Override
    public void insererCompte(Compte compte) throws MobileMoneyException {
        if (tableComptes.containsKey(compte.getNumero())) {
            throw new MobileMoneyException("Numéro de compte déjà existant : " + compte.getNumero());
        }
        tableComptes.put(compte.getNumero(), compte);
    }

    @Override
    public Compte trouverParNumero(String numero) throws MobileMoneyException {
        return tableComptes.get(numero);  // null si absent (le service convertit en exception)
    }

    @Override
    public List<Compte> lireTous() throws MobileMoneyException {
        return new ArrayList<>(tableComptes.values());
    }

    @Override
    public void mettreAJourSolde(String numero, double nouveauSolde) throws MobileMoneyException {
        Compte c = tableComptes.get(numero);
        if (c == null) throw new MobileMoneyException.CompteInexistantException(numero);
        // En vrai DAO JDBC : UPDATE comptes SET solde = ? WHERE numero = ?
        // Ici on met à jour l'objet en mémoire (le service l'a déjà mis à jour via debiter/crediter)
    }

    @Override
    public void mettreAJourStatut(String numero, boolean actif) throws MobileMoneyException {
        Compte c = tableComptes.get(numero);
        if (c == null) throw new MobileMoneyException.CompteInexistantException(numero);
        c.setActif(actif);
    }

    @Override
    public void mettreAJourMotDePasse(String numero, String motDePasseHash) throws MobileMoneyException {
        Compte c = tableComptes.get(numero);
        if (c == null) throw new MobileMoneyException.CompteInexistantException(numero);
        // En vrai DAO JDBC : UPDATE comptes SET mot_de_passe = ? WHERE numero = ?
        c.setMotDePasseHash(motDePasseHash);
    }

    @Override
    public void enregistrerTransaction(Transaction transaction) throws MobileMoneyException {
        tableTransactions.add(transaction);
    }

    @Override
    public Transaction executerTransfert(String numeroSource, String numeroDestination,
                                         double montant, double commission)
            throws MobileMoneyException {

        // Simulation de la transaction SQL atomique
        Compte source = tableComptes.get(numeroSource);
        Compte dest   = tableComptes.get(numeroDestination);

        if (source == null) throw new MobileMoneyException.CompteInexistantException(numeroSource);
        if (dest == null)   throw new MobileMoneyException.CompteInexistantException(numeroDestination);

        // Opérations atomiques simulées
        source.debiter(montant + commission);
        dest.crediter(montant);

        Transaction t = new Transaction(TypeTransaction.TRANSFERT,
                numeroSource, numeroDestination, montant, commission, true);
        tableTransactions.add(t);
        return t;
    }

    @Override
    public List<Transaction> lireTransactionsParCompte(String numeroCompte) throws MobileMoneyException {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : tableTransactions) {
            if (numeroCompte.equals(t.getNumeroSource()) ||
                    numeroCompte.equals(t.getNumeroDestination())) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public List<Transaction> lireToutesLesTransactions() throws MobileMoneyException {
        return new ArrayList<>(tableTransactions);
    }
}
