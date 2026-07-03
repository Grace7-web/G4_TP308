package cm.mobilemoney.vue;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Écran de récupération de mot de passe en 2 étapes :
 *
 *  Étape 1 — L'utilisateur saisit son numéro de compte. Si un compte
 *            existe et possède une question secrète, elle est affichée.
 *  Étape 2 — L'utilisateur répond à la question secrète et choisit un
 *            nouveau mot de passe. Si la réponse est correcte, le mot
 *            de passe est réinitialisé via {@code ICompteService}.
 *
 * Utilise un CardLayout interne pour passer d'une étape à l'autre sans
 * perturber la navigation globale de {@link MainFrame}.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class MotDePasseOubliePanel extends JPanel {

    private static final int LARGEUR_CHAMP = 300;
    private static final String ETAPE_NUMERO   = "ETAPE_NUMERO";
    private static final String ETAPE_REPONSE  = "ETAPE_REPONSE";

    private final CardLayout cardLayoutInterne = new CardLayout();
    private final JPanel panneauEtapes = new JPanel(cardLayoutInterne);

    private final JLabel labelMessageEtape1 = new JLabel(" ");
    private final JLabel labelMessageEtape2 = new JLabel(" ");
    private final JLabel labelQuestion = new JLabel(" ");

    private final JTextField champNumero = new JTextField();
    private final JPasswordField champNouveauMdp = new JPasswordField();
    private final JPasswordField champConfirmationMdp = new JPasswordField();
    private final JTextField champReponse = new JTextField();

    /** Numéro de compte validé à l'étape 1, réutilisé à l'étape 2. */
    private String numeroCompteEnCours;

    public MotDePasseOubliePanel(MainFrame parent) {
        super(new GridBagLayout());
        setBackground(Theme.BLEU_FONCE);

        ICompteService service = parent.getCompteService();

        panneauEtapes.add(construireEtapeNumero(service, parent), ETAPE_NUMERO);
        panneauEtapes.add(construireEtapeReponse(service, parent), ETAPE_REPONSE);
        panneauEtapes.setOpaque(false);

        add(panneauEtapes);
        cardLayoutInterne.show(panneauEtapes, ETAPE_NUMERO);
    }

    // ── Étape 1 : saisie du numéro de compte ──────────────────────────

    private JPanel construireEtapeNumero(ICompteService service, MainFrame parent) {
        JPanel carte = nouvelleCarte();

        JLabel titre = creerTitre("Mot de passe oublié");
        JLabel sousTitre = creerSousTitre("Entrez votre numéro de compte");

        JLabel labelNumero = creerLabelChamp("Numéro de compte");
        styliserChamp(champNumero);

        labelMessageEtape1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessageEtape1.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelMessageEtape1.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnContinuer = creerBoutonPrincipal("CONTINUER");
        JButton btnRetour = creerBoutonLien("← Retour à la connexion");

        btnContinuer.addActionListener(e -> {
            String numero = champNumero.getText().trim();
            if (numero.isEmpty()) {
                afficherErreur(labelMessageEtape1, "Veuillez saisir un numéro de compte.");
                return;
            }
            try {
                String question = service.obtenirQuestionSecrete(numero);
                numeroCompteEnCours = numero;
                labelQuestion.setText("<html><div style='text-align:center; width:260px;'>"
                        + question + "</div></html>");
                champReponse.setText("");
                champNouveauMdp.setText("");
                champConfirmationMdp.setText("");
                labelMessageEtape2.setText(" ");
                cardLayoutInterne.show(panneauEtapes, ETAPE_REPONSE);
            } catch (MobileMoneyException ex) {
                afficherErreur(labelMessageEtape1, ex.getMessage());
            }
        });

        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_LOGIN));

        carte.add(titre);
        carte.add(Box.createVerticalStrut(8));
        carte.add(creerTraitAccent());
        carte.add(Box.createVerticalStrut(10));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(24));
        carte.add(labelNumero);
        carte.add(Box.createVerticalStrut(5));
        carte.add(champNumero);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelMessageEtape1);
        carte.add(Box.createVerticalStrut(14));
        carte.add(btnContinuer);
        carte.add(Box.createVerticalStrut(10));
        carte.add(btnRetour);

        JPanel enveloppe = new JPanel(new GridBagLayout());
        enveloppe.setOpaque(false);
        enveloppe.add(carte);
        return enveloppe;
    }

    // ── Étape 2 : question secrète + nouveau mot de passe ─────────────

    private JPanel construireEtapeReponse(ICompteService service, MainFrame parent) {
        JPanel carte = nouvelleCarte();

        JLabel titre = creerTitre("Vérification d'identité");
        JLabel sousTitre = creerSousTitre("Répondez à votre question secrète");

        labelQuestion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelQuestion.setForeground(Theme.BLEU_FONCE);
        labelQuestion.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelQuestion.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel labelReponse = creerLabelChamp("Votre réponse");
        styliserChamp(champReponse);

        JLabel labelNouveauMdp = creerLabelChamp("Nouveau mot de passe");
        styliserChamp(champNouveauMdp);

        JLabel labelConfirmation = creerLabelChamp("Confirmer le mot de passe");
        styliserChamp(champConfirmationMdp);

        labelMessageEtape2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessageEtape2.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelMessageEtape2.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnReinitialiser = creerBoutonPrincipal("RÉINITIALISER");
        JButton btnRetourEtape1 = creerBoutonLien("← Changer de compte");
        JButton btnRetourLogin = creerBoutonLien("Retour à la connexion");

        btnReinitialiser.addActionListener(e -> {
            String reponse = champReponse.getText().trim();
            String nouveauMdp = new String(champNouveauMdp.getPassword());
            String confirmation = new String(champConfirmationMdp.getPassword());

            if (reponse.isEmpty()) {
                afficherErreur(labelMessageEtape2, "Veuillez répondre à la question.");
                return;
            }
            if (nouveauMdp.isEmpty()) {
                afficherErreur(labelMessageEtape2, "Veuillez saisir un nouveau mot de passe.");
                return;
            }
            if (!nouveauMdp.equals(confirmation)) {
                afficherErreur(labelMessageEtape2, "Les deux mots de passe ne correspondent pas.");
                return;
            }

            try {
                service.reinitialiserMotDePasse(numeroCompteEnCours, reponse, nouveauMdp);

                JOptionPane.showMessageDialog(
                        this,
                        "Votre mot de passe a été réinitialisé avec succès !\n"
                                + "Vous pouvez maintenant vous connecter.",
                        "Mot de passe mis à jour",
                        JOptionPane.INFORMATION_MESSAGE
                );

                parent.afficherCarte(MainFrame.CARTE_LOGIN);

            } catch (MobileMoneyException ex) {
                afficherErreur(labelMessageEtape2, ex.getMessage());
            }
        });

        btnRetourEtape1.addActionListener(e -> cardLayoutInterne.show(panneauEtapes, ETAPE_NUMERO));
        btnRetourLogin.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_LOGIN));

        carte.add(titre);
        carte.add(Box.createVerticalStrut(8));
        carte.add(creerTraitAccent());
        carte.add(Box.createVerticalStrut(10));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(20));
        carte.add(labelQuestion);
        carte.add(Box.createVerticalStrut(16));
        carte.add(labelReponse);
        carte.add(Box.createVerticalStrut(5));
        carte.add(champReponse);
        carte.add(Box.createVerticalStrut(14));
        carte.add(labelNouveauMdp);
        carte.add(Box.createVerticalStrut(5));
        carte.add(champNouveauMdp);
        carte.add(Box.createVerticalStrut(14));
        carte.add(labelConfirmation);
        carte.add(Box.createVerticalStrut(5));
        carte.add(champConfirmationMdp);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelMessageEtape2);
        carte.add(Box.createVerticalStrut(14));
        carte.add(btnReinitialiser);
        carte.add(Box.createVerticalStrut(10));
        carte.add(btnRetourEtape1);
        carte.add(Box.createVerticalStrut(6));
        carte.add(btnRetourLogin);

        JScrollPane defilement = new JScrollPane(carte);
        defilement.setBorder(BorderFactory.createEmptyBorder());
        defilement.getVerticalScrollBar().setUnitIncrement(16);
        defilement.setOpaque(false);
        defilement.getViewport().setOpaque(false);

        JPanel enveloppe = new JPanel(new BorderLayout());
        enveloppe.setOpaque(false);
        enveloppe.add(defilement, BorderLayout.CENTER);
        return enveloppe;
    }

    // ── Fabriques de composants réutilisables ─────────────────────────

    private JPanel nouvelleCarte() {
        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(new EmptyBorder(35, 45, 30, 45));
        return carte;
    }

    private JLabel creerTitre(String texte) {
        JLabel titre = new JLabel(texte);
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(Theme.BLEU_FONCE);
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        return titre;
    }

    private JLabel creerSousTitre(String texte) {
        JLabel sousTitre = new JLabel(texte);
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sousTitre;
    }

    private JPanel creerTraitAccent() {
        JPanel trait = new JPanel();
        trait.setBackground(Theme.ORANGE_VIF);
        trait.setMaximumSize(new Dimension(50, 4));
        trait.setPreferredSize(new Dimension(50, 4));
        trait.setAlignmentX(Component.CENTER_ALIGNMENT);
        return trait;
    }

    private JButton creerBoutonPrincipal(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setFont(Theme.POLICE_BOUTON);
        bouton.setBackground(Theme.ORANGE_VIF);
        bouton.setForeground(Theme.BLANC);
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setOpaque(true);
        bouton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bouton.setMaximumSize(new Dimension(LARGEUR_CHAMP, 42));
        bouton.setPreferredSize(new Dimension(LARGEUR_CHAMP, 42));
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return bouton;
    }

    private JButton creerBoutonLien(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setFont(Theme.POLICE_SOUS_TITRE);
        bouton.setForeground(Theme.TEXTE_GRIS);
        bouton.setBorderPainted(false);
        bouton.setContentAreaFilled(false);
        bouton.setFocusPainted(false);
        bouton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return bouton;
    }

    private JLabel creerLabelChamp(String texte) {
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

    private void afficherErreur(JLabel label, String message) {
        label.setForeground(Theme.ROUGE_ERREUR);
        label.setText(message);
    }
}
