package cm.mobilemoney.vue;

import static javax.swing.SwingConstants.*;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.service.CompteService;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Écran de transfert d'argent entre deux comptes.
 *
 * Le compte source est pré-rempli (et verrouillé) si un compte est
 * connecté ; le compte destination doit toujours être saisi.
 * Une commission de {@code CompteService.TAUX_COMMISSION_TRANSFERT}%
 * est prélevée sur le compte source en plus du montant transféré.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class TransfertPanel extends JPanel {

    private static final int LARGEUR_CHAMP = 300;

    private final JTextField champSource = new JTextField();
    private final JTextField champDestination = new JTextField();
    private final JTextField champMontant = new JTextField();
    private final JLabel labelMessage = new JLabel(" ");
    private final JPanel zoneRecap = new JPanel();
    private final JLabel labelRecapTexte = new JLabel(" ");

    private double montantConfirme;
    private String sourceConfirmee;
    private String destinationConfirmee;
    private boolean etapeRecap = false;

    public TransfertPanel(MainFrame parent) {
        super(new GridBagLayout());
        setBackground(Theme.FOND_CLAIR);

        ICompteService service = parent.getCompteService();

        JPanel carte = new JPanel();
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBackground(Theme.BLANC);
        carte.setBorder(new EmptyBorder(35, 45, 30, 45));

        JLabel symbole = new JLabel("→", CENTER);
        symbole.setFont(new Font("Segoe UI", Font.BOLD, 36));
        symbole.setForeground(Theme.ORANGE_VIF);
        symbole.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titre = new JLabel("Effectuer un transfert");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(Theme.BLEU_FONCE);
        titre.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sousTitre = new JLabel(String.format(
                "Commission de %.1f%% appliquée", CompteService.TAUX_COMMISSION_TRANSFERT));
        sousTitre.setFont(Theme.POLICE_SOUS_TITRE);
        sousTitre.setForeground(Theme.TEXTE_GRIS);
        sousTitre.setAlignmentX(CENTER_ALIGNMENT);

        styliserChamp(champSource);
        styliserChamp(champDestination);
        styliserChamp(champMontant);

        Compte compteConnecte = parent.getCompteConnecte();
        if (compteConnecte != null) {
            champSource.setText(compteConnecte.getNumero());
            champSource.setEditable(false);
            champSource.setBackground(new Color(240, 243, 248));
        }

        labelMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelMessage.setAlignmentX(CENTER_ALIGNMENT);
        labelMessage.setHorizontalAlignment(CENTER);

        zoneRecap.setLayout(new BoxLayout(zoneRecap, BoxLayout.Y_AXIS));
        zoneRecap.setBackground(new Color(255, 246, 235));
        zoneRecap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ORANGE_VIF, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));
        zoneRecap.setMaximumSize(new Dimension(LARGEUR_CHAMP, 110));
        zoneRecap.setAlignmentX(CENTER_ALIGNMENT);
        zoneRecap.setVisible(false);

        labelRecapTexte.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelRecapTexte.setForeground(Theme.TEXTE_SOMBRE);
        labelRecapTexte.setAlignmentX(CENTER_ALIGNMENT);
        zoneRecap.add(labelRecapTexte);

        JButton btnAction = new JButton("VÉRIFIER LE TRANSFERT");
        btnAction.setFont(Theme.POLICE_BOUTON);
        btnAction.setBackground(Theme.ORANGE_VIF);
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
                confirmerTransfert(service, parent, btnAction);
            }
        });

        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_ACCUEIL));

        carte.add(symbole);
        carte.add(Box.createVerticalStrut(4));
        carte.add(titre);
        carte.add(Box.createVerticalStrut(4));
        carte.add(sousTitre);
        carte.add(Box.createVerticalStrut(24));
        carte.add(creerLabelChamp("Compte source"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champSource);
        carte.add(Box.createVerticalStrut(14));
        carte.add(creerLabelChamp("Compte destinataire"));
        carte.add(Box.createVerticalStrut(5));
        carte.add(champDestination);
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

    private void preparerRecap(JButton btnAction) {
        String source = champSource.getText().trim();
        String destination = champDestination.getText().trim();
        String montantTexte = champMontant.getText().trim();

        if (source.isEmpty() || destination.isEmpty()) {
            afficherErreur("Veuillez renseigner les deux comptes.");
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

        if (montant % 500 != 0) {
            afficherErreur("Le montant doit être un multiple de 500 FCFA.");
            return;
        }

        this.sourceConfirmee = source;
        this.destinationConfirmee = destination;
        this.montantConfirme = montant;
        this.etapeRecap = true;

        double commission = montant * CompteService.TAUX_COMMISSION_TRANSFERT / 100.0;
        double total = montant + commission;

        labelMessage.setText(" ");
        labelRecapTexte.setText(String.format(
                "<html>De : <b>%s</b><br/>Vers : <b>%s</b><br/>Montant : <b>%,.0f FCFA</b><br/>"
                        + "Commission : <b>%,.0f FCFA</b><br/>Total prélevé : <b>%,.0f FCFA</b></html>",
                source, destination, montant, commission, total));
        zoneRecap.setVisible(true);
        btnAction.setText("CONFIRMER LE TRANSFERT");
        revalidate();
    }

    private void confirmerTransfert(ICompteService service, MainFrame parent, JButton btnAction) {
        btnAction.setEnabled(false);
        labelMessage.setForeground(Theme.TEXTE_GRIS);
        labelMessage.setText("Transfert en cours...");

        SwingWorker<Transaction, Void> worker = new SwingWorker<Transaction, Void>() {
            @Override
            protected Transaction doInBackground() throws Exception {
                return service.transferer(sourceConfirmee, destinationConfirmee, montantConfirme);
            }

            @Override
            protected void done() {
                btnAction.setEnabled(true);
                try {
                    Transaction transaction = get();
                    JOptionPane.showMessageDialog(
                            TransfertPanel.this,
                            String.format("Transfert effectué avec succès !\n\nMontant : %,.0f FCFA\nDe : %s\nVers : %s",
                                    transaction.getMontant(), sourceConfirmee, destinationConfirmee),
                            "Transfert réussi",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    reinitialiser(btnAction);
                    parent.afficherCarte(MainFrame.CARTE_ACCUEIL);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String errorMsg = (cause instanceof MobileMoneyException) ? cause.getMessage() : "Erreur de transfert.";
                    afficherErreur(errorMsg);
                    zoneRecap.setVisible(false);
                    etapeRecap = false;
                    btnAction.setText("VÉRIFIER LE TRANSFERT");
                    revalidate();
                }
            }
        };
        worker.execute();
    }

    private void reinitialiser(JButton btnAction) {
        champDestination.setText("");
        champMontant.setText("");
        labelMessage.setText(" ");
        zoneRecap.setVisible(false);
        etapeRecap = false;
        btnAction.setText("VÉRIFIER LE TRANSFERT");
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