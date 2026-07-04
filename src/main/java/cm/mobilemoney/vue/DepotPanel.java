package cm.mobilemoney.vue;

import static javax.swing.SwingConstants.*;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Écran de dépôt d'argent sur un compte.
 *
 * Comportement :
 *  - Si un compte est connecté, le numéro de compte est pré-rempli
 *    et verrouillé (lecture seule).
 *  - Sinon (mode invité), l'utilisateur doit saisir le numéro manuellement.
 *  - Avant la confirmation finale, un récapitulatif (montant, compte
 *    destinataire) s'affiche pour limiter les erreurs de saisie.
 *  - Toutes les règles métier (montant multiple de 500, compte actif...)
 *    sont déléguées à {@code ICompteService.deposer()} — l'IHM ne fait
 *    que relayer les exceptions sous forme de message lisible.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class DepotPanel extends JPanel {

    private static final int LARGEUR_CHAMP = 300;

    private final JTextField champCompte = new JTextField();
    private final JTextField champMontant = new JTextField();
    private final JLabel labelMessage = new JLabel(" ");
    private final JPanel zoneRecap = new JPanel();
    private final JLabel labelRecapTexte = new JLabel(" ");

    private double montantConfirme;
    private String numeroConfirme;
    private boolean etapeRecap = false;

    public DepotPanel(MainFrame parent) {
        super(new GridBagLayout());
        setBackground(Theme.FOND_CLAIR);

        ICompteService service = parent.getCompteService();

        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(new EmptyBorder(35, 45, 30, 45));

        // ── En-tête avec icône et titre ────────────────────────────────────────
        JLabel symbole = new JLabel("+", CENTER);
        symbole.setFont(new Font("Segoe UI", Font.BOLD, 36));
        symbole.setForeground(Theme.VERT_SUCCES);
        symbole.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titre = new JLabel("Effectuer un dépôt");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(Theme.BLEU_FONCE);
        titre.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel("Créditez un compte Mobile Money");
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(CENTER_ALIGNMENT);

        // ── Champs ─────────────────────────────────────────────────────────────
        styliserChamp(champCompte);
        styliserChamp(champMontant);

        Compte compteConnecte = parent.getCompteConnecte();
        if (compteConnecte != null) {
            champCompte.setText(compteConnecte.getNumero());
            champCompte.setEditable(false);
            champCompte.setBackground(new Color(240, 243, 248));
        }

        labelMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessage.setAlignmentX(CENTER_ALIGNMENT);
        labelMessage.setHorizontalAlignment(CENTER);

        // ── Zone de récapitulatif (cachée par défaut) ──────────────────────────
        zoneRecap.setLayout(new BoxLayout(zoneRecap, BoxLayout.Y_AXIS));
        zoneRecap.setBackground(new Color(240, 248, 242));
        zoneRecap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.VERT_SUCCES, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));
        zoneRecap.setMaximumSize(new Dimension(LARGEUR_CHAMP, 80));
        zoneRecap.setAlignmentX(CENTER_ALIGNMENT);
        zoneRecap.setVisible(false);

        labelRecapTexte.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelRecapTexte.setForeground(Theme.TEXTE_SOMBRE);
        labelRecapTexte.setAlignmentX(CENTER_ALIGNMENT);
        zoneRecap.add(labelRecapTexte);

        // ── Bouton principal (texte dynamique selon l'étape) ───────────────────
        JButton btnAction = new JButton("VÉRIFIER LE DÉPÔT");
        btnAction.setFont(Theme.POLICE_BOUTON);
        btnAction.setBackground(Theme.VERT_SUCCES);
        btnAction.setForeground(Theme.BLANC);
        btnAction.setFocusPainted(false);
        btnAction.setBorderPainted(false);
        btnAction.setOpaque(true);
        btnAction.setAlignmentX(CENTER_ALIGNMENT);
        btnAction.setMaximumSize(new Dimension(LARGEUR_CHAMP, 42));
        btnAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnRetour = new JButton("← Retour à l'accueil");
        btnRetour.setFont(Theme.POLICE_SOUS_TITRE);
        btnRetour.setForeground(Theme.TEXTE_GRIS);
        btnRetour.setBorderPainted(false);
        btnRetour.setContentAreaFilled(false);
        btnRetour.setFocusPainted(false);
        btnRetour.setAlignmentX(CENTER_ALIGNMENT);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnAction.addActionListener(e -> {
            if (!etapeRecap) {
                preparerRecap(btnAction);
            } else {
                confirmerDepot(service, parent, btnAction);
            }
        });

        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_ACCUEIL));

        // ── Assemblage ─────────────────────────────────────────────────────────
        carte.add(symbole);
        carte.add(Box.createVerticalStrut(4));
        carte.add(titre);
        carte.add(Box.createVerticalStrut(4));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(26));
        carte.add(creerLabelChamp("Numéro de compte"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champCompte);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Montant (FCFA, multiple de 500)"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champMontant);
        carte.add(Box.createVerticalStrut(10));
        carte.add(labelMessage);
        carte.add(Box.createVerticalStrut(8));
        carte.add(zoneRecap);
        carte.add(Box.createVerticalStrut(14));
        carte.add(btnAction);
        carte.add(Box.createVerticalStrut(10));
        carte.add(btnRetour);

        add(carte);
    }

    /**
     * Étape 1 : valide le format des champs et affiche un récapitulatif
     * avant la confirmation réelle (la validation métier complète n'a
     * lieu qu'à la confirmation, via le service).
     */
    private void preparerRecap(JButton btnAction) {
        String numero = champCompte.getText().trim();
        String montantTexte = champMontant.getText().trim();

        if (numero.isEmpty()) {
            afficherErreur("Veuillez saisir le numéro de compte.");
            return;
        }
        if (montantTexte.isEmpty()) {
            afficherErreur("Veuillez saisir un montant.");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantTexte);
        } catch (NumberFormatException ex) {
            afficherErreur("Le montant doit être un nombre valide.");
            return;
        }

        if (montant <= 0) {
            afficherErreur("Le montant doit être positif.");
            return;
        }

        this.numeroConfirme = numero;
        this.montantConfirme = montant;
        this.etapeRecap = true;

        labelMessage.setText(" ");
        labelRecapTexte.setText(String.format(
                "<html>Vous allez créditer le compte <b>%s</b><br/>d'un montant de <b>%,.0f FCFA</b></html>",
                numero, montant));
        zoneRecap.setVisible(true);
        btnAction.setText("CONFIRMER LE DÉPÔT");
        revalidate();
    }

    /**
     * Étape 2 : envoie réellement l'opération au service métier
     * et affiche le résultat (succès ou message d'erreur métier).
     */
    private void confirmerDepot(ICompteService service, MainFrame parent, JButton btnAction) {
        btnAction.setEnabled(false);
        labelMessage.setForeground(Theme.TEXTE_GRIS);
        labelMessage.setText("Dépôt en cours...");

        SwingWorker<Transaction, Void> worker = new SwingWorker<Transaction, Void>() {
            @Override
            protected Transaction doInBackground() throws Exception {
                return service.deposer(numeroConfirme, montantConfirme);
            }

            @Override
            protected void done() {
                btnAction.setEnabled(true);
                try {
                    Transaction transaction = get();
                    JOptionPane.showMessageDialog(
                            DepotPanel.this,
                            String.format("Dépôt effectué avec succès !\n\nMontant : %,.0f FCFA\nCompte : %s",
                                    transaction.getMontant(), numeroConfirme),
                            "Dépôt réussi",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    reinitialiser(btnAction);
                    parent.afficherCarte(MainFrame.CARTE_ACCUEIL);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String errorMsg = (cause instanceof MobileMoneyException) ? cause.getMessage() : "Erreur lors du dépôt.";
                    afficherErreur(errorMsg);
                    zoneRecap.setVisible(false);
                    etapeRecap = false;
                    btnAction.setText("VÉRIFIER LE DÉPÔT");
                    revalidate();
                }
            }
        };
        worker.execute();
    }

    private void reinitialiser(JButton btnAction) {
        champMontant.setText("");
        labelMessage.setText(" ");
        zoneRecap.setVisible(false);
        etapeRecap = false;
        btnAction.setText("VÉRIFIER LE DÉPÔT");
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
        champ.setMaximumSize(new Dimension(LARGEUR_CHAMP, 36));
        champ.setPreferredSize(new Dimension(LARGEUR_CHAMP, 36));
        champ.setAlignmentX(CENTER_ALIGNMENT);
        champ.setHorizontalAlignment(CENTER);
        champ.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }
}