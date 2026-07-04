package cm.mobilemoney.vue;

import static javax.swing.SwingConstants.*;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class InscriptionPanel extends JPanel {

    private static final int LARGEUR_CHAMP = 300;

    private static final String[] QUESTIONS_SECRETES = {
            "Quel est le nom de votre premier animal de compagnie ?",
            "Quelle est votre ville de naissance ?",
            "Quel est le prénom de votre mère ?",
            "Quel est le nom de votre école primaire ?",
            "Quel est votre plat préféré ?"
    };

    private final JTextField champNom = new JTextField();
    private final JTextField champTelephone = new JTextField();
    private final JPasswordField champMdp = new JPasswordField();
    private final JPasswordField champConfirmationMdp = new JPasswordField();
    private final JComboBox<String> comboQuestionSecrete = new JComboBox<>(QUESTIONS_SECRETES);
    private final JTextField champReponseSecrete = new JTextField();
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
        titre.setAlignmentX(CENTER_ALIGNMENT);

        JPanel traitAccent = new JPanel();
        traitAccent.setBackground(Theme.ORANGE_VIF);
        traitAccent.setMaximumSize(new Dimension(50, 4));
        traitAccent.setPreferredSize(new Dimension(50, 4));
        traitAccent.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Renseignez vos informations");
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(CENTER_ALIGNMENT);

        styliserChamp(champNom);
        styliserChamp(champTelephone);
        styliserChamp(champMdp);
        styliserChamp(champConfirmationMdp);
        styliserChamp(champReponseSecrete);
        styliserChamp(champDepotInitial);

        comboQuestionSecrete.setMaximumSize(new Dimension(LARGEUR_CHAMP, 34));
        comboQuestionSecrete.setPreferredSize(new Dimension(LARGEUR_CHAMP, 34));
        comboQuestionSecrete.setAlignmentX(CENTER_ALIGNMENT);
        comboQuestionSecrete.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        labelMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessage.setAlignmentX(CENTER_ALIGNMENT);
        labelMessage.setHorizontalAlignment(CENTER);

        JButton btnCreer = new JButton("CRÉER MON COMPTE");
        btnCreer.setFont(Theme.POLICE_BOUTON);
        btnCreer.setBackground(Theme.ORANGE_VIF);
        btnCreer.setForeground(Theme.BLANC);
        btnCreer.setFocusPainted(false);
        btnCreer.setBorderPainted(false);
        btnCreer.setOpaque(true);
        btnCreer.setAlignmentX(CENTER_ALIGNMENT);
        btnCreer.setMaximumSize(new Dimension(LARGEUR_CHAMP, 42));
        btnCreer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnRetour = new JButton("← Retour à la connexion");
        btnRetour.setFont(Theme.POLICE_SOUS_TITRE);
        btnRetour.setForeground(Theme.TEXTE_GRIS);
        btnRetour.setBorderPainted(false);
        btnRetour.setContentAreaFilled(false);
        btnRetour.setFocusPainted(false);
        btnRetour.setAlignmentX(CENTER_ALIGNMENT);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnCreer.addActionListener(e -> creerCompte(service, parent, btnCreer));
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
        carte.add(creerLabelChamp("Confirmer le mot de passe"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champConfirmationMdp);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Question secrète (pour récupération)"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(comboQuestionSecrete);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Votre réponse"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champReponseSecrete);
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

        JScrollPane defilement = new JScrollPane(carte);
        defilement.setBorder(BorderFactory.createEmptyBorder());
        defilement.getVerticalScrollBar().setUnitIncrement(16);
        defilement.setOpaque(false);
        defilement.getViewport().setOpaque(false);

       GridBagConstraints gbc = new GridBagConstraints();
       gbc.fill = GridBagConstraints.BOTH;
       gbc.weightx = 1;
       gbc.weighty = 1;
       add(defilement, gbc);
    }

    
    private void creerCompte(ICompteService service, MainFrame parent, JButton btnCreer) {
        String nom = champNom.getText().trim();
        String telephone = champTelephone.getText().trim();
        String mdp = new String(champMdp.getPassword());
        String confirmationMdp = new String(champConfirmationMdp.getPassword());
        String question = (String) comboQuestionSecrete.getSelectedItem();
        String reponse = champReponseSecrete.getText().trim();
        String depotTexte = champDepotInitial.getText().trim();

        if (nom.isEmpty()) {
            afficherErreur("Le nom complet est obligatoire.");
            return;
        }
        if (telephone.isEmpty()) {
            afficherErreur("Le numéro de téléphone est obligatoire.");
            return;
        }
        if (mdp.isEmpty()) {
            afficherErreur("Le mot de passe est obligatoire.");
            return;
        }
        if (mdp.length() < 4) {
            afficherErreur("Le mot de passe doit contenir au moins 4 caractères.");
            return;
        }
        if (!mdp.equals(confirmationMdp)) {
            afficherErreur("Les deux mots de passe ne correspondent pas.");
            return;
        }
        if (reponse.isEmpty()) {
            afficherErreur("Veuillez répondre à la question secrète.");
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

        btnCreer.setEnabled(false);
        labelMessage.setForeground(Theme.TEXTE_GRIS);
        labelMessage.setText("Création en cours...");

        SwingWorker<Compte, Void> worker = new SwingWorker<Compte, Void>() {
            @Override
            protected Compte doInBackground() throws Exception {
                return service.creerCompte(nom, depotInitial, mdp, question, reponse);
            }

            @Override
            protected void done() {
                btnCreer.setEnabled(true);
                try {
                    Compte nouveauCompte = get();
                    JOptionPane.showMessageDialog(
                            InscriptionPanel.this,
                            "Compte créé avec succès !\n\nVotre numéro de compte est :\n"
                                    + nouveauCompte.getNumero()
                                    + "\n\nConservez-le précieusement pour vous connecter.",
                            "Inscription réussie",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    parent.definirCompteConnecte(nouveauCompte);
                    parent.afficherCarte(MainFrame.CARTE_ACCUEIL);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String errorMsg = (cause instanceof MobileMoneyException || cause instanceof IllegalArgumentException) 
                            ? cause.getMessage() : "Erreur lors de la création du compte.";
                    afficherErreur(errorMsg);
                }
            }
        };
        worker.execute();
    }

    private void afficherErreur(String message) {
        labelMessage.setForeground(Theme.ROUGE_ERREUR);
        labelMessage.setText(message);
    }

    private JLabel creerLabelChamp(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(Theme.POLICE_LABEL);
        label.setForeground(Theme.TEXTE_SOMBRE);
        label.setAlignmentX(CENTER_ALIGNMENT);
        return label;
    }

    private void styliserChamp(JTextField champ) {
        champ.setMaximumSize(new Dimension(LARGEUR_CHAMP, 34));
        champ.setPreferredSize(new Dimension(LARGEUR_CHAMP, 34));
        champ.setAlignmentX(CENTER_ALIGNMENT);
        champ.setHorizontalAlignment(CENTER);
        champ.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }
}