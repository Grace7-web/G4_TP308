package cm.mobilemoney.vue;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Écran de connexion affiché au lancement de l'application.
 *
 * MISE À JOUR : le mot de passe est désormais réellement vérifié via
 * {@code ICompteService.seConnecter()} (hachage SHA-256 comparé côté
 * service). Un lien "Mot de passe oublié ?" permet de basculer vers
 * {@link MotDePasseOubliePanel}.
 *
 *  - Le champ "Numéro de compte" + "Mot de passe" sont vérifiés via
 *    {@code ICompteService.seConnecter()}.
 *  - Un lien "Créer un compte" permet de basculer vers {@link InscriptionPanel}.
 *  - Un bouton "Continuer sans compte" permet d'explorer l'application
 *    sans connexion, pour ne pas bloquer le développement/la démo.
 *
 * Design : thème bleu foncé + orange (cf. {@link Theme}). Tous les champs
 * sont centrés horizontalement grâce à GridBagLayout sur le panneau racine
 * et à BoxLayout (Y_AXIS) + alignement CENTER sur la carte.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class LoginPanel extends JPanel {

    private static final int LARGEUR_CHAMP = 280;
    private int tentativesEchouees = 0;

    public LoginPanel(MainFrame parent) {
        super(new GridBagLayout());
        setBackground(Theme.BLEU_FONCE);

        ICompteService service = parent.getCompteService();

        // ── Carte centrale blanche contenant le formulaire ─────────────
        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(new EmptyBorder(40, 45, 35, 45));

        // ── Logo / Titre de l'application ───────────────────────────────
        JLabel logo = new JLabel("MOBILE MONEY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(Theme.BLEU_FONCE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Petit accent orange sous le logo
        JPanel traitAccent = new JPanel();
        traitAccent.setBackground(Theme.ORANGE_VIF);
        traitAccent.setMaximumSize(new Dimension(50, 4));
        traitAccent.setPreferredSize(new Dimension(50, 4));
        traitAccent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Connexion à votre espace");
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Champ Numéro de compte (centré) ─────────────────────────────
        JLabel labelCompte = creerLabelChamp("Numéro de compte");
        JTextField champCompte = new JTextField();
        styliserChamp(champCompte);

        // ── Champ Mot de passe (centré) ─────────────────────────────────
        JLabel labelMdp = creerLabelChamp("Mot de passe");
        JPasswordField champMdp = new JPasswordField();
        styliserChamp(champMdp);

        // ── Lien "Mot de passe oublié ?" ────────────────────────────────
        JButton btnMdpOublie = new JButton("Mot de passe oublié ?");
        styliserLien(btnMdpOublie, Theme.BLEU_MOYEN);
        btnMdpOublie.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMdpOublie.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Label d'erreur ───────────────────────────────────────────────
        JLabel labelErreur = new JLabel(" ");
        labelErreur.setForeground(Theme.ROUGE_ERREUR);
        labelErreur.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelErreur.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelErreur.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Bouton de connexion (orange, plein, centré) ──────────────────
        JButton btnConnexion = new JButton("SE CONNECTER");
        btnConnexion.setFont(Theme.POLICE_BOUTON);
        btnConnexion.setBackground(Theme.ORANGE_VIF);
        btnConnexion.setForeground(Theme.BLANC);
        btnConnexion.setFocusPainted(false);
        btnConnexion.setBorderPainted(false);
        btnConnexion.setOpaque(true);
        btnConnexion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnexion.setMaximumSize(new Dimension(LARGEUR_CHAMP, 42));
        btnConnexion.setPreferredSize(new Dimension(LARGEUR_CHAMP, 42));
        btnConnexion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Bouton "Continuer sans compte" (texte simple) ────────────────
        JButton btnInvite = new JButton("Continuer sans compte");
        styliserLien(btnInvite, Theme.TEXTE_GRIS);

        // ── Séparateur visuel ──────────────────────────────────────────
        JSeparator separateur = new JSeparator();
        separateur.setMaximumSize(new Dimension(LARGEUR_CHAMP, 1));
        separateur.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Lien "Créer un compte" ───────────────────────────────────────
        JLabel labelPasDeCompte = new JLabel("Vous n'avez pas encore de compte ?");
        labelPasDeCompte.setFont(Theme.POLICE_SOUS_TITRE);
        labelPasDeCompte.setForeground(Theme.TEXTE_GRIS);
        labelPasDeCompte.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnInscription = new JButton("Créer un compte");
        styliserLien(btnInscription, Theme.BLEU_MOYEN);
        btnInscription.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // ── Action de connexion ───────────────────────────────────────────
        Runnable actionConnexion = () -> {
            if (tentativesEchouees >= 3) {
                return;
            }

            String numero = champCompte.getText().trim();
            String mdp = new String(champMdp.getPassword());

            if (numero.isEmpty() || mdp.isEmpty()) {
                labelErreur.setText("Veuillez saisir le compte et le mot de passe.");
                return;
            }

            try {
                Compte compte = service.seConnecter(numero, mdp);
                tentativesEchouees = 0;
                parent.definirCompteConnecte(compte);
                parent.afficherCarte(MainFrame.CARTE_ACCUEIL);
            } catch (MobileMoneyException e) {
                tentativesEchouees++;
                if (tentativesEchouees >= 3) {
                    labelErreur.setText("Erreur : Bouton bloqué après 3 tentatives.");
                    btnConnexion.setEnabled(false);
                    btnConnexion.setBackground(Theme.TEXTE_GRIS);
                } else {
                    int restants = 3 - tentativesEchouees;
                    labelErreur.setText(e.getMessage() + " (" + restants + " essai(s) restant(s))");
                }
            }
        };

        btnConnexion.addActionListener(e -> actionConnexion.run());
        btnInvite.addActionListener(e -> {
            parent.definirCompteConnecte(null);
            parent.afficherCarte(MainFrame.CARTE_ACCUEIL);
        });
        btnInscription.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_INSCRIPTION));
        btnMdpOublie.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_MDP_OUBLIE));

        champMdp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionConnexion.run();
                }
            }
        });

        // ── Assemblage vertical de la carte (tout centré) ────────────────
        carte.add(logo);
        carte.add(Box.createVerticalStrut(8));
        carte.add(traitAccent);
        carte.add(Box.createVerticalStrut(10));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(28));
        carte.add(labelCompte);
        carte.add(Box.createVerticalStrut(5));
        carte.add(champCompte);
        carte.add(Box.createVerticalStrut(16));
        carte.add(labelMdp);
        carte.add(Box.createVerticalStrut(5));
        carte.add(champMdp);
        carte.add(Box.createVerticalStrut(6));
        carte.add(btnMdpOublie);
        carte.add(Box.createVerticalStrut(4));
        carte.add(labelErreur);
        carte.add(Box.createVerticalStrut(14));
        carte.add(btnConnexion);
        carte.add(Box.createVerticalStrut(10));
        carte.add(btnInvite);
        carte.add(Box.createVerticalStrut(20));
        carte.add(separateur);
        carte.add(Box.createVerticalStrut(16));
        carte.add(labelPasDeCompte);
        carte.add(Box.createVerticalStrut(6));
        carte.add(btnInscription);

        add(carte);
    }

    // ── Utilitaires de style ─────────────────────────────────────────────

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

    private void styliserLien(JButton bouton, Color couleur) {
        bouton.setFont(Theme.POLICE_SOUS_TITRE);
        bouton.setForeground(couleur);
        bouton.setBorderPainted(false);
        bouton.setContentAreaFilled(false);
        bouton.setFocusPainted(false);
        bouton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
