package cm.mobilemoney.vue;


import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Fenêtre principale de l'application Mobile Money.
 *
 * Architecture de navigation :
 *  - Une JMenuBar (cachée sur les écrans publics Login/Inscription/MdpOublié)
 *  - Une zone centrale gérée par un CardLayout
 *  - Écrans statiques (construits une fois) : Login, Accueil
 *  - Écrans dynamiques (reconstruits à chaque visite) : Inscription, MdpOublié,
 *    Dépôt, Retrait, Transfert, Historique, ListeComptes, CreerCompte
 *    (rebuild nécessaire pour repartir d'un état propre à chaque visite)
 *
 * MISE À JOUR : ajout de la carte CARTE_MDP_OUBLIE pour l'écran de
 * récupération de mot de passe (voir {@link MotDePasseOubliePanel}).
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class MainFrame extends JFrame {

    public static final String CARTE_LOGIN         = "LOGIN";
    public static final String CARTE_INSCRIPTION   = "INSCRIPTION";
    public static final String CARTE_MDP_OUBLIE    = "MDP_OUBLIE";
    public static final String CARTE_ACCUEIL       = "ACCUEIL";
    public static final String CARTE_DEPOT         = "DEPOT";
    public static final String CARTE_RETRAIT       = "RETRAIT";
    public static final String CARTE_TRANSFERT     = "TRANSFERT";
    public static final String CARTE_HISTORIQUE    = "HISTORIQUE";
    public static final String CARTE_COMPTES       = "COMPTES";
    public static final String CARTE_CREER_COMPTE  = "CREER_COMPTE";

    private final ICompteService compteService;
    private Compte compteConnecte;

    private final CardLayout cardLayout;
    private final JPanel panneauPrincipal;
    private final JLabel labelStatut;
    private final JMenuBar barreMenus;
    private final AccueilPanel accueilPanel;

    private final Map<String, JPanel> cartesDynamiques = new HashMap<>();

    public MainFrame(ICompteService compteService) {
        super("Mobile Money - Gestion des comptes");

        if (compteService == null) {
            throw new IllegalArgumentException("Le service métier ne peut pas être null.");
        }
        this.compteService = compteService;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);

        barreMenus = creerBarreMenus();
        setJMenuBar(barreMenus);
        barreMenus.setVisible(false);

        cardLayout = new CardLayout();
        panneauPrincipal = new JPanel(cardLayout);

        panneauPrincipal.add(new LoginPanel(this), CARTE_LOGIN);
        accueilPanel = new AccueilPanel(this);
        panneauPrincipal.add(accueilPanel, CARTE_ACCUEIL);

        labelStatut = new JLabel("  Veuillez vous connecter.");
        labelStatut.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        labelStatut.setBackground(Theme.FOND_CLAIR);
        labelStatut.setOpaque(true);
        labelStatut.setForeground(Theme.TEXTE_GRIS);

        setLayout(new BorderLayout());
        add(panneauPrincipal, BorderLayout.CENTER);
        add(labelStatut,       BorderLayout.SOUTH);

        afficherCarte(CARTE_LOGIN);
    }

    // ── Menus ──────────────────────────────────────────────────────────

    private JMenuBar creerBarreMenus() {
        JMenuBar barre = new JMenuBar();
        barre.setBackground(Theme.BLEU_FONCE);
        barre.add(creerMenuFichier());
        barre.add(creerMenuOperations());
        barre.add(creerMenuConsultation());
        barre.add(creerMenuAide());
        return barre;
    }

    private JMenu creerMenuFichier() {
        JMenu menu = new JMenu("Fichier");
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem itemAccueil = new JMenuItem("Accueil");
        itemAccueil.addActionListener(e -> afficherCarte(CARTE_ACCUEIL));

        JMenuItem itemDeconnexion = new JMenuItem("Déconnexion");
        itemDeconnexion.addActionListener(e -> deconnecter());

        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> quitterApplication());

        menu.add(itemAccueil);
        menu.addSeparator();
        menu.add(itemDeconnexion);
        menu.add(itemQuitter);
        return menu;
    }

    private JMenu creerMenuOperations() {
        JMenu menu = new JMenu("Opérations");
        menu.setMnemonic(KeyEvent.VK_O);

        JMenuItem itemDepot = new JMenuItem("Effectuer un dépôt");
        itemDepot.addActionListener(e -> afficherCarte(CARTE_DEPOT));

        JMenuItem itemRetrait = new JMenuItem("Effectuer un retrait");
        itemRetrait.addActionListener(e -> afficherCarte(CARTE_RETRAIT));

        JMenuItem itemTransfert = new JMenuItem("Effectuer un transfert");
        itemTransfert.addActionListener(e -> afficherCarte(CARTE_TRANSFERT));

        menu.add(itemDepot);
        menu.add(itemRetrait);
        menu.add(itemTransfert);
        return menu;
    }

    private JMenu creerMenuConsultation() {
        JMenu menu = new JMenu("Consultation");
        menu.setMnemonic(KeyEvent.VK_C);

        JMenuItem itemComptes = new JMenuItem("Liste des comptes");
        itemComptes.addActionListener(e -> afficherCarte(CARTE_COMPTES));

        JMenuItem itemHistorique = new JMenuItem("Historique des transactions");
        itemHistorique.addActionListener(e -> afficherCarte(CARTE_HISTORIQUE));

        JMenuItem itemNouveauCompte = new JMenuItem("Nouveau compte");
        itemNouveauCompte.addActionListener(e -> afficherCarte(CARTE_CREER_COMPTE));

        menu.add(itemComptes);
        menu.add(itemHistorique);
        menu.addSeparator();
        menu.add(itemNouveauCompte);
        return menu;
    }

    private JMenu creerMenuAide() {
        JMenu menu = new JMenu("Aide");
        menu.setMnemonic(KeyEvent.VK_A);

        JMenuItem itemAPropos = new JMenuItem("À propos");
        itemAPropos.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Mobile Money - Projet 4\n"
                        + "Application de gestion de comptes mobiles.\n"
                        + "Équipe IHM Swing — ICT308",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE
        ));
        menu.add(itemAPropos);
        return menu;
    }

    // ── Navigation ────────────────────────────────────────────────────

    public void afficherCarte(String nomCarte) {
        boolean ecranPublic = CARTE_LOGIN.equals(nomCarte)
                || CARTE_INSCRIPTION.equals(nomCarte)
                || CARTE_MDP_OUBLIE.equals(nomCarte);
        barreMenus.setVisible(!ecranPublic);

        switch (nomCarte) {
            case CARTE_ACCUEIL -> accueilPanel.rafraichir(compteConnecte);
            case CARTE_INSCRIPTION ->
                    reconstruire(nomCarte, new InscriptionPanel(this));
            case CARTE_MDP_OUBLIE ->
                    reconstruire(nomCarte, new MotDePasseOubliePanel(this));
            case CARTE_DEPOT ->
                    reconstruire(nomCarte, new DepotPanel(this));
            case CARTE_RETRAIT ->
                    reconstruire(nomCarte, new RetraitPanel(this));
            case CARTE_TRANSFERT ->
                    reconstruire(nomCarte, new TransfertPanel(this));
            case CARTE_HISTORIQUE ->
                    reconstruire(nomCarte, new HistoriquePanel(this));
            case CARTE_COMPTES ->
                    reconstruire(nomCarte, new ListeComptesPanel(this));
            case CARTE_CREER_COMPTE ->
                    reconstruire(nomCarte, new CreerComptePanel(this));
            default -> { }
        }

        cardLayout.show(panneauPrincipal, nomCarte);
        panneauPrincipal.repaint(); 
        definirStatut(libelleStatut(nomCarte));
    }

    private void reconstruire(String nomCarte, JPanel nouveauPanel) {
        JPanel ancien = cartesDynamiques.get(nomCarte);
        if (ancien != null) {
            panneauPrincipal.remove(ancien);
        }
        panneauPrincipal.add(nouveauPanel, nomCarte);
        cartesDynamiques.put(nomCarte, nouveauPanel);
        panneauPrincipal.revalidate();
        panneauPrincipal.repaint();
    }

    private String libelleStatut(String nomCarte) {
        return switch (nomCarte) {
            case CARTE_LOGIN -> "Veuillez vous connecter.";
            case CARTE_INSCRIPTION -> "Création d'un nouveau compte.";
            case CARTE_MDP_OUBLIE -> "Récupération du mot de passe.";
            case CARTE_ACCUEIL -> compteConnecte != null
                    ? "Connecté en tant que " + compteConnecte.getTitulaire()
                    : "Mode invité";
            default -> "Écran actif : " + nomCarte;
        };
    }

    public void definirStatut(String message) {
        labelStatut.setText("  " + message);
    }

    public ICompteService getCompteService() {
        return compteService;
    }

    public void definirCompteConnecte(Compte compte) {
        this.compteConnecte = compte;
    }

    public Compte getCompteConnecte() {
        return compteConnecte;
    }

    private void deconnecter() {
        compteConnecte = null;
        afficherCarte(CARTE_LOGIN);
    }

    private void quitterApplication() {
        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment quitter l'application ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choix == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
