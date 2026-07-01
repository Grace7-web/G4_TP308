package cm.mobilemoney.vue;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Écran de création rapide d'un compte (accessible depuis le menu
 * Consultation → Nouveau compte).
 *
 * Différence avec {@link InscriptionPanel} : cet écran est destiné
 * à un administrateur qui crée un compte pour quelqu'un d'autre,
 * depuis l'intérieur de l'application (après connexion).
 * Il ne demande que le strict nécessaire : nom du titulaire et
 * dépôt initial, qui sont les seuls champs supportés par
 * {@code ICompteService.creerCompte()}.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class CreerComptePanel extends JPanel {

    private static final int LARGEUR_CHAMP = 300;

    private final JTextField champNom = new JTextField();
    private final JTextField champDepot = new JTextField();
    private final JLabel labelMessage = new JLabel(" ");
    private final JPanel zoneResultat = new JPanel();
    private final JLabel labelNumeroGenere = new JLabel();

    public CreerComptePanel(MainFrame parent) {
        super(new GridBagLayout());
        setBackground(Theme.FOND_CLAIR);

        ICompteService service = parent.getCompteService();

        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(new EmptyBorder(35, 45, 30, 45));

        // ── En-tête ────────────────────────────────────────────────────────────
        JLabel symbole = new JLabel("＋", SwingConstants.CENTER);
        symbole.setFont(new Font("Segoe UI", Font.BOLD, 34));
        symbole.setForeground(Theme.BLEU_MOYEN);
        symbole.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titre = new JLabel("Nouveau compte");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(Theme.BLEU_FONCE);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel(
                String.format("Dépôt minimum : %,.0f FCFA", Compte.SOLDE_MINIMUM));
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Champs ─────────────────────────────────────────────────────────────
        styliserChamp(champNom);
        styliserChamp(champDepot);

        labelMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelMessage.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Zone résultat (numéro de compte généré) ───────────────────────────
        zoneResultat.setLayout(new BoxLayout(zoneResultat, BoxLayout.Y_AXIS));
        zoneResultat.setBackground(new Color(235, 245, 255));
        zoneResultat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BLEU_MOYEN, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));
        zoneResultat.setMaximumSize(new Dimension(LARGEUR_CHAMP, 80));
        zoneResultat.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoneResultat.setVisible(false);

        JLabel labelTitreNumero = new JLabel("Numéro de compte attribué :", SwingConstants.CENTER);
        labelTitreNumero.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelTitreNumero.setForeground(Theme.TEXTE_GRIS);
        labelTitreNumero.setAlignmentX(Component.CENTER_ALIGNMENT);

        labelNumeroGenere.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelNumeroGenere.setForeground(Theme.BLEU_FONCE);
        labelNumeroGenere.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelNumeroGenere.setHorizontalAlignment(SwingConstants.CENTER);

        zoneResultat.add(labelTitreNumero);
        zoneResultat.add(Box.createVerticalStrut(5));
        zoneResultat.add(labelNumeroGenere);

        // ── Boutons ────────────────────────────────────────────────────────────
        JButton btnCreer = new JButton("CRÉER LE COMPTE");
        btnCreer.setFont(Theme.POLICE_BOUTON);
        btnCreer.setBackground(Theme.BLEU_MOYEN);
        btnCreer.setForeground(Theme.BLANC);
        btnCreer.setFocusPainted(false);
        btnCreer.setBorderPainted(false);
        btnCreer.setOpaque(true);
        btnCreer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreer.setMaximumSize(new Dimension(LARGEUR_CHAMP, 42));
        btnCreer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnNouvelleCreation = new JButton("Créer un autre compte");
        btnNouvelleCreation.setFont(Theme.POLICE_SOUS_TITRE);
        btnNouvelleCreation.setForeground(Theme.BLEU_MOYEN);
        btnNouvelleCreation.setBorderPainted(false);
        btnNouvelleCreation.setContentAreaFilled(false);
        btnNouvelleCreation.setFocusPainted(false);
        btnNouvelleCreation.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNouvelleCreation.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNouvelleCreation.setVisible(false);

        JButton btnRetour = new JButton("← Retour à l'accueil");
        btnRetour.setFont(Theme.POLICE_SOUS_TITRE);
        btnRetour.setForeground(Theme.TEXTE_GRIS);
        btnRetour.setBorderPainted(false);
        btnRetour.setContentAreaFilled(false);
        btnRetour.setFocusPainted(false);
        btnRetour.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Actions ────────────────────────────────────────────────────────────
        btnCreer.addActionListener(e -> {
            String nom = champNom.getText().trim();
            String depotTexte = champDepot.getText().trim();

            if (nom.isEmpty()) {
                afficherErreur("Le nom du titulaire est obligatoire.");
                return;
            }
            if (depotTexte.isEmpty()) {
                afficherErreur("Le dépôt initial est obligatoire.");
                return;
            }

            double depot;
            try {
                depot = Double.parseDouble(depotTexte);
            } catch (NumberFormatException ex) {
                afficherErreur("Le dépôt doit être un nombre valide.");
                return;
            }

            try {
                Compte nouveauCompte = service.creerCompte(nom, depot);

                labelMessage.setText(" ");
                labelNumeroGenere.setText(nouveauCompte.getNumero());
                zoneResultat.setVisible(true);
                btnCreer.setVisible(false);
                btnNouvelleCreation.setVisible(true);
                parent.definirStatut("Compte créé : " + nouveauCompte.getNumero());
                revalidate();

            } catch (MobileMoneyException | IllegalArgumentException ex) {
                afficherErreur(ex.getMessage());
            }
        });

        btnNouvelleCreation.addActionListener(e -> {
            champNom.setText("");
            champDepot.setText("");
            labelMessage.setText(" ");
            zoneResultat.setVisible(false);
            btnCreer.setVisible(true);
            btnNouvelleCreation.setVisible(false);
            revalidate();
        });

        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_ACCUEIL));

        // ── Assemblage ─────────────────────────────────────────────────────────
        carte.add(symbole);
        carte.add(Box.createVerticalStrut(4));
        carte.add(titre);
        carte.add(Box.createVerticalStrut(4));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(28));
        carte.add(creerLabel("Nom complet du titulaire"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champNom);
        carte.add(Box.createVerticalStrut(16));
        carte.add(creerLabel("Dépôt initial (FCFA, multiple de 500)"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champDepot);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelMessage);
        carte.add(Box.createVerticalStrut(8));
        carte.add(zoneResultat);
        carte.add(Box.createVerticalStrut(14));
        carte.add(btnCreer);
        carte.add(btnNouvelleCreation);
        carte.add(Box.createVerticalStrut(10));
        carte.add(btnRetour);

        add(carte);
    }

    private void afficherErreur(String message) {
        labelMessage.setForeground(Theme.ROUGE_ERREUR);
        labelMessage.setText(message);
    }

    private JLabel creerLabel(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(Theme.POLICE_LABEL);
        label.setForeground(Theme.TEXTE_SOMBRE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void styliserChamp(JTextField champ) {
        champ.setMaximumSize(new Dimension(LARGEUR_CHAMP, 36));
        champ.setPreferredSize(new Dimension(LARGEUR_CHAMP, 36));
        champ.setAlignmentX(Component.CENTER_ALIGNMENT);
        champ.setHorizontalAlignment(SwingConstants.CENTER);
        champ.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }
}
