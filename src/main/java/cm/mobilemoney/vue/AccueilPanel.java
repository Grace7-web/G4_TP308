package cm.mobilemoney.vue;

import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Écran d'accueil affiché après la connexion.
 *
 * Structure (de haut en bas) :
 *  1. Bandeau dégradé bleu foncé avec message de bienvenue et solde en grand
 *  2. Grille de "raccourcis" colorés vers les opérations principales
 *     (Dépôt, Retrait, Transfert, Historique)
 *  3. Zone statistiques : nombre de transactions, dernière activité,
 *     totaux dépôts/retraits — calculés à partir de
 *     {@code ICompteService.obtenirHistorique()} pour le compte connecté
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class AccueilPanel extends JPanel {

    private final JLabel labelBienvenue;
    private final JLabel labelSolde;
    private final JLabel labelDetails;

    private final JLabel valeurNbTransactions;
    private final JLabel valeurDerniereActivite;
    private final JLabel valeurTotalDepots;
    private final JLabel valeurTotalRetraits;
    private MainFrame parentFrame;

    public AccueilPanel(MainFrame parent) {
        super(new BorderLayout());
        setBackground(Theme.FOND_CLAIR);

        // ── Bandeau supérieur en dégradé bleu foncé ───────────────────────────
        JPanel bandeau = Theme.creerPanelDegrade(Theme.BLEU_FONCE, Theme.BLEU_NUIT);
        bandeau.setLayout(new BoxLayout(bandeau, BoxLayout.Y_AXIS));
        bandeau.setBorder(new EmptyBorder(28, 40, 28, 40));

        labelBienvenue = new JLabel("Bienvenue");
        labelBienvenue.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        labelBienvenue.setForeground(Theme.BLANC);
        labelBienvenue.setAlignmentX(Component.CENTER_ALIGNMENT);

        labelDetails = new JLabel(" ");
        labelDetails.setFont(Theme.POLICE_SOUS_TITRE);
        labelDetails.setForeground(new Color(170, 190, 215));
        labelDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelSoldeTitre = new JLabel("SOLDE DISPONIBLE");
        labelSoldeTitre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelSoldeTitre.setForeground(Theme.ORANGE_VIF);
        labelSoldeTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        labelSolde = new JLabel("-- FCFA");
        labelSolde.setFont(Theme.POLICE_MONTANT);
        labelSolde.setForeground(Theme.OR);
        labelSolde.setAlignmentX(Component.CENTER_ALIGNMENT);

        bandeau.add(labelBienvenue);
        bandeau.add(Box.createVerticalStrut(4));
        bandeau.add(labelDetails);
        bandeau.add(Box.createVerticalStrut(18));
        bandeau.add(labelSoldeTitre);
        bandeau.add(Box.createVerticalStrut(6));
        bandeau.add(labelSolde);

        // ── Corps : raccourcis + statistiques ──────────────────────────────────
        JPanel corps = new JPanel();
        corps.setLayout(new BoxLayout(corps, BoxLayout.Y_AXIS));
        corps.setBackground(Theme.FOND_CLAIR);
        corps.setBorder(new EmptyBorder(28, 40, 28, 40));

        // -- Raccourcis --
        JPanel grille = new JPanel(new GridLayout(1, 4, 20, 0));
        grille.setBackground(Theme.FOND_CLAIR);
        grille.setAlignmentX(Component.CENTER_ALIGNMENT);
        grille.setMaximumSize(new Dimension(760, 130));
        grille.setPreferredSize(new Dimension(760, 130));

        grille.add(creerCarteAction("Dépôt", "+", Theme.VERT_SUCCES,
                () -> parent.afficherCarte(MainFrame.CARTE_DEPOT)));
        grille.add(creerCarteAction("Retrait", "-", Theme.ROUGE_ERREUR,
                () -> parent.afficherCarte(MainFrame.CARTE_RETRAIT)));
        grille.add(creerCarteAction("Transfert", "→", Theme.ORANGE_VIF,
                () -> parent.afficherCarte(MainFrame.CARTE_TRANSFERT)));
        grille.add(creerCarteAction("Historique", "≡", Theme.BLEU_MOYEN,
                () -> parent.afficherCarte(MainFrame.CARTE_HISTORIQUE)));

        // -- Titre section statistiques --
        JLabel titreStats = new JLabel("Aperçu de votre activité");
        titreStats.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titreStats.setForeground(Theme.BLEU_FONCE);
        titreStats.setAlignmentX(Component.CENTER_ALIGNMENT);
        titreStats.setBorder(new EmptyBorder(35, 0, 14, 0));

        // -- Grille de statistiques --
        JPanel grilleStats = new JPanel(new GridLayout(1, 4, 16, 0));
        grilleStats.setBackground(Theme.FOND_CLAIR);
        grilleStats.setAlignmentX(Component.CENTER_ALIGNMENT);
        grilleStats.setMaximumSize(new Dimension(760, 95));
        grilleStats.setPreferredSize(new Dimension(760, 95));

        valeurNbTransactions = creerValeurStat();
        valeurDerniereActivite = creerValeurStat();
        valeurTotalDepots = creerValeurStat();
        valeurTotalRetraits = creerValeurStat();

        grilleStats.add(creerCarteStat("Transactions", valeurNbTransactions, Theme.BLEU_MOYEN));
        grilleStats.add(creerCarteStat("Dernière activité", valeurDerniereActivite, Theme.TEXTE_GRIS));
        grilleStats.add(creerCarteStat("Total déposé", valeurTotalDepots, Theme.VERT_SUCCES));
        grilleStats.add(creerCarteStat("Total retiré", valeurTotalRetraits, Theme.ROUGE_ERREUR));

        corps.add(grille);
        corps.add(titreStats);
        corps.add(grilleStats);
        corps.add(Box.createVerticalGlue());

        add(bandeau, BorderLayout.NORTH);
        add(corps, BorderLayout.CENTER);

        // Première mise à jour avec l'état courant (peut être "non connecté")
        this.parentFrame = parent;
        rafraichir(parent.getCompteConnecte(), parent.getCompteService());
    }

    // ── Fabrication des cartes de raccourci ───────────────────────────────────

    private JPanel creerCarteAction(String libelle, String symbole, Color couleur, Runnable actionClic) {
        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, couleur),
                new EmptyBorder(20, 10, 16, 10)
        ));
        carte.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel labelSymbole = new JLabel(symbole, SwingConstants.CENTER);
        labelSymbole.setFont(new Font("Segoe UI", Font.BOLD, 32));
        labelSymbole.setForeground(couleur);
        labelSymbole.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelTexte = new JLabel(libelle, SwingConstants.CENTER);
        labelTexte.setFont(Theme.POLICE_CARTE_TITRE);
        labelTexte.setForeground(Theme.TEXTE_SOMBRE);
        labelTexte.setAlignmentX(Component.CENTER_ALIGNMENT);

        carte.add(labelSymbole);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelTexte);

        carte.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                actionClic.run();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                carte.setBackground(new Color(248, 250, 253));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                carte.setBackground(Theme.BLANC);
            }
        });

        return carte;
    }

    // ── Fabrication des cartes de statistique ─────────────────────────────────

    private JLabel creerValeurStat() {
        JLabel label = new JLabel("--", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel creerCarteStat(String libelle, JLabel labelValeur, Color couleurValeur) {
        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 232, 238)),
                new EmptyBorder(14, 10, 14, 10)
        ));

        labelValeur.setForeground(couleurValeur);

        JLabel labelTitre = new JLabel(libelle, SwingConstants.CENTER);
        labelTitre.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelTitre.setForeground(Theme.TEXTE_GRIS);
        labelTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        carte.add(labelValeur);
        carte.add(Box.createVerticalStrut(4));
        carte.add(labelTitre);

        return carte;
    }

    /**
     * Met à jour l'affichage en fonction du compte actuellement connecté :
     * solde, message de bienvenue, et statistiques calculées à partir
     * de l'historique de transactions du compte.
     *
     * @param compte         Le compte connecté, ou null si mode invité
     * @param compteService  Service métier pour récupérer l'historique
     */
    public void rafraichir(Compte compte, ICompteService compteService) {
        if (compte == null) {
            labelBienvenue.setText("Bienvenue sur Mobile Money");
            labelDetails.setText("Mode invité — connectez-vous pour voir votre solde");
            labelSolde.setText("-- FCFA");
            reinitialiserStats();
            return;
        }

        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        labelBienvenue.setText("Bonjour, " + compte.getTitulaire());
        labelDetails.setText("Compte n° " + compte.getNumero()
                + "  ·  Client depuis le " + compte.getDateCreation().format(fmtDate));
        labelSolde.setText(String.format("%,.0f FCFA", compte.getSolde()));

        try {
            List<Transaction> historique = compteService.obtenirHistorique(compte.getNumero());
            mettreAJourStatistiques(historique);
        } catch (Exception ex) {
            reinitialiserStats();
        }
    }

    /** Permet à MainFrame de rafraîchir sans repasser explicitement le service. */
    public void rafraichir(Compte compte) {
        rafraichir(compte, parentFrame != null ? parentFrame.getCompteService() : null);
    }

    private void mettreAJourStatistiques(List<Transaction> historique) {
        valeurNbTransactions.setText(String.valueOf(historique.size()));

        if (historique.isEmpty()) {
            valeurDerniereActivite.setText("Aucune");
            valeurTotalDepots.setText("0 FCFA");
            valeurTotalRetraits.setText("0 FCFA");
            return;
        }

        // La liste est déjà triée du plus récent au plus ancien (cf. Transaction.compareTo)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        valeurDerniereActivite.setText(historique.get(0).getDateHeure().format(fmt));

        double totalDepots = 0;
        double totalRetraits = 0;
        for (Transaction t : historique) {
            switch (t.getType()) {
                case DEPOT -> totalDepots += t.getMontant();
                case RETRAIT -> totalRetraits += t.getMontant();
                default -> { /* transferts non comptabilisés ici */ }
            }
        }

        valeurTotalDepots.setText(String.format("%,.0f FCFA", totalDepots));
        valeurTotalRetraits.setText(String.format("%,.0f FCFA", totalRetraits));
    }

    private void reinitialiserStats() {
        valeurNbTransactions.setText("--");
        valeurDerniereActivite.setText("--");
        valeurTotalDepots.setText("--");
        valeurTotalRetraits.setText("--");
    }
}