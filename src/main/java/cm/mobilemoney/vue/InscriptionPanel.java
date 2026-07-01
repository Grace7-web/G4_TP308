package cm.mobilemoney.vue;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Écran d'inscription permettant de créer un nouveau compte Mobile Money.
 *
 * Champs demandés : Nom complet, Téléphone, Mot de passe, Dépôt initial.
 *
 * ⚠️ NOTE IMPORTANTE POUR L'ÉQUIPE PERSISTANCE :
 * La classe métier {@code Compte} ne contient actuellement que
 * (numero, titulaire, solde, dateCreation, actif) — pas de téléphone
 * ni de mot de passe. Ces deux champs sont donc collectés ici à titre
 * d'ergonomie/démonstration mais ne sont PAS persistés pour l'instant.
 * Si l'authentification par mot de passe est requise dans la version
 * finale, il faudra étendre Compte + ICompteDAO en conséquence.
 *
 * Seuls le nom et le dépôt initial sont transmis à
 * {@code ICompteService.creerCompte(titulaire, soldeInitial)}.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class InscriptionPanel extends JPanel {

    private static final int LARGEUR_CHAMP = 300;

    private final JTextField champNom = new JTextField();
    private final JTextField champTelephone = new JTextField();
    private final JPasswordField champMdp = new JPasswordField();
    private final JTextField champDepotInitial = new JTextField();
    private final JLabel labelMessage = new JLabel(" ");

    public InscriptionPanel(MainFrame parent) {
        super(new GridBagLayout());
        setBackground(Theme.BLEU_FONCE);

        ICompteService service = parent.getCompteService();

        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(new EmptyBorder(35, 45, 30, 45));

        JLabel titre = new JLabel("Créer un compte");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titre.setForeground(Theme.BLEU_FONCE);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel traitAccent = new JPanel();
        traitAccent.setBackground(Theme.ORANGE_VIF);
        traitAccent.setMaximumSize(new Dimension(50, 4));
        traitAccent.setPreferredSize(new Dimension(50, 4));
        traitAccent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Renseignez vos informations");
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        styliserChamp(champNom);
        styliserChamp(champTelephone);
        styliserChamp(champMdp);
        styliserChamp(champDepotInitial);

        labelMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelMessage.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnCreer = new JButton("CRÉER MON COMPTE");
        btnCreer.setFont(Theme.POLICE_BOUTON);
        btnCreer.setBackground(Theme.ORANGE_VIF);
        btnCreer.setForeground(Theme.BLANC);
        btnCreer.setFocusPainted(false);
        btnCreer.setBorderPainted(false);
        btnCreer.setOpaque(true);
        btnCreer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreer.setMaximumSize(new Dimension(LARGEUR_CHAMP, 42));
        btnCreer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnRetour = new JButton("← Retour à la connexion");
        btnRetour.setFont(Theme.POLICE_SOUS_TITRE);
        btnRetour.setForeground(Theme.TEXTE_GRIS);
        btnRetour.setBorderPainted(false);
        btnRetour.setContentAreaFilled(false);
        btnRetour.setFocusPainted(false);
        btnRetour.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnCreer.addActionListener(e -> creerCompte(service, parent));
        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_LOGIN));

        carte.add(titre);
        carte.add(Box.createVerticalStrut(8));
        carte.add(traitAccent);
        carte.add(Box.createVerticalStrut(10));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(24));
        carte.add(creerLabelChamp("Nom complet"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champNom);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Téléphone"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champTelephone);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Mot de passe"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champMdp);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Dépôt initial (FCFA, min. " + (int) Compte.SOLDE_MINIMUM + ")"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champDepotInitial);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelMessage);
        carte.add(Box.createVerticalStrut(14));
        carte.add(btnCreer);
        carte.add(Box.createVerticalStrut(10));
        carte.add(btnRetour);

        add(carte);
    }

    /**
     * Valide les champs saisis et crée le compte via le service métier.
     * Le téléphone et le mot de passe sont collectés mais non transmis
     * au service (non supportés par la classe Compte actuelle).
     */
    private void creerCompte(ICompteService service, MainFrame parent) {
        String nom = champNom.getText().trim();
        String telephone = champTelephone.getText().trim();
        String depotTexte = champDepotInitial.getText().trim();

        if (nom.isEmpty()) {
            afficherErreur("Le nom complet est obligatoire.");
            return;
        }
        if (telephone.isEmpty()) {
            afficherErreur("Le numéro de téléphone est obligatoire.");
            return;
        }
        if (depotTexte.isEmpty()) {
            afficherErreur("Le dépôt initial est obligatoire.");
            return;
        }

        double depotInitial;
        try {
            depotInitial = Double.parseDouble(depotTexte);
        } catch (NumberFormatException ex) {
            afficherErreur("Le dépôt initial doit être un nombre valide.");
            return;
        }

        try {
            Compte nouveauCompte = service.creerCompte(nom, depotInitial);

            JOptionPane.showMessageDialog(
                    this,
                    "Compte créé avec succès !\n\nVotre numéro de compte est :\n"
                            + nouveauCompte.getNumero()
                            + "\n\nConservez-le précieusement pour vous connecter.",
                    "Inscription réussie",
                    JOptionPane.INFORMATION_MESSAGE
            );

            parent.definirCompteConnecte(nouveauCompte);
            parent.afficherCarte(MainFrame.CARTE_ACCUEIL);

        } catch (MobileMoneyException | IllegalArgumentException ex) {
            afficherErreur(ex.getMessage());
        }
    }

    private void afficherErreur(String message) {
        labelMessage.setForeground(Theme.ROUGE_ERREUR);
        labelMessage.setText(message);
    }

    private JLabel creerLabelChamp(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(Theme.POLICE_LABEL);
        label.setForeground(Theme.TEXTE_SOMBRE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void styliserChamp(JTextField champ) {
        champ.setMaximumSize(new Dimension(LARGEUR_CHAMP, 34));
        champ.setPreferredSize(new Dimension(LARGEUR_CHAMP, 34));
        champ.setAlignmentX(Component.CENTER_ALIGNMENT);
        champ.setHorizontalAlignment(SwingConstants.CENTER);
        champ.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }
}