package cm.mobilemoney.vue;

import cm.mobilemoney.service.CompteDAOMock;
import cm.mobilemoney.service.CompteService;
import cm.mobilemoney.service.ICompteService;
import cm.mobilemoney.dao.CompteDAO;

import javax.swing.*;

/**
 * Point d'entrée de l'application Mobile Money (interface graphique).
 *
 * Respecte la règle fondamentale de Swing vue en semaine 6 du cours :
 * toute création de composants Swing doit se faire dans l'Event Dispatch
 * Thread (EDT), via SwingUtilities.invokeLater().
 *
 * IMPORTANT pour l'équipe :
 * Le service est actuellement construit avec {@code CompteDAOMock}
 * (données en mémoire, pour pouvoir développer l'IHM sans attendre la BD).
 * Dès que l'Équipe Persistance livre son {@code CompteDAO} JDBC réel
 * (dans cm.mobilemoney.dao), il suffira de remplacer la ligne :
 *
 *     ICompteService service = new CompteService(new CompteDAOMock());
 * par :
 *     ICompteService service = new CompteService(new CompteDAO());
 *
 * Aucune autre modification ne sera nécessaire dans les écrans Swing,
 * grâce au découplage assuré par l'interface ICompteService.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class LanceurApplication {

    public static void main(String[] args) {

        // Création du service métier (mock en attendant le DAO JDBC réel)
        ICompteService compteService = new CompteService(new CompteDAO());

        // TOUJOURS lancer l'interface graphique dans l'EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame fenetrePrincipale = new MainFrame(compteService);
            fenetrePrincipale.setVisible(true);
        });
    }
}
