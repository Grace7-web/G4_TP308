package cm.mobilemoney.vue;

import static javax.swing.SwingConstants.*;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Écran affichant la liste de tous les comptes du système.
 *
 * Fonctionnalités :
 *  - Tableau avec colonnes : Numéro, Titulaire, Solde, Date création, Statut
 *  - Lignes colorées : vert si actif, rouge si bloqué
 *  - Bouton "Bloquer / Débloquer" sur la ligne sélectionnée
 *  - Bouton "Actualiser" pour recharger depuis le service
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class ListeComptesPanel extends JPanel {

    private static final String[] COLONNES = {
            "Numéro de compte", "Titulaire", "Solde (FCFA)", "Date création", "Statut"
    };

    private final DefaultTableModel modeleTable;
    private final JTable table;
    private final JLabel labelNbComptes;
    private List<Compte> comptesCharges;

    public ListeComptesPanel(MainFrame parent) {
        super(new BorderLayout());
        setBackground(Theme.FOND_CLAIR);
        setBorder(new EmptyBorder(25, 30, 20, 30));

        ICompteService service = parent.getCompteService();

        // ── En-tête ────────────────────────────────────────────────────────────
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(Theme.FOND_CLAIR);
        entete.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titreZone = new JPanel();
        titreZone.setLayout(new BoxLayout(titreZone, BoxLayout.Y_AXIS));
        titreZone.setBackground(Theme.FOND_CLAIR);

        JLabel titre = new JLabel("Liste des comptes");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(Theme.BLEU_FONCE);

        labelNbComptes = new JLabel("Chargement...");
        labelNbComptes.setFont(Theme.POLICE_SOUS_TITRE);
        labelNbComptes.setForeground(Theme.TEXTE_GRIS);

        titreZone.add(titre);
        titreZone.add(Box.createVerticalStrut(3));
        titreZone.add(labelNbComptes);

        // Boutons d'action à droite
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setBackground(Theme.FOND_CLAIR);

        JButton btnActualiser = creerBouton("⟳ Actualiser", Theme.BLEU_MOYEN);
        JButton btnBloquer = creerBouton("🔒 Bloquer / Débloquer", Theme.ORANGE_VIF);

        boutons.add(btnBloquer);
        boutons.add(btnActualiser);

        entete.add(titreZone, BorderLayout.WEST);
        entete.add(boutons, BorderLayout.EAST);

        // ── Table ──────────────────────────────────────────────────────────────
        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(modeleTable);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(210, 228, 250));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(Theme.BLEU_FONCE);
        table.getTableHeader().setForeground(Theme.BLANC);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setDefaultRenderer(Object.class, new RendererStatutCompte());

        // Centrer colonnes Solde et Statut
        DefaultTableCellRenderer centreRenderer = new DefaultTableCellRenderer();
        centreRenderer.setHorizontalAlignment(CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centreRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(new RendererStatutCompte());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 236)));
        scrollPane.getViewport().setBackground(Theme.BLANC);

        // ── Barre d'actions en bas ─────────────────────────────────────────────
        JButton btnRetour = new JButton("← Retour à l'accueil");
        btnRetour.setFont(Theme.POLICE_SOUS_TITRE);
        btnRetour.setForeground(Theme.TEXTE_GRIS);
        btnRetour.setBorderPainted(false);
        btnRetour.setContentAreaFilled(false);
        btnRetour.setFocusPainted(false);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panneauBas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panneauBas.setBackground(Theme.FOND_CLAIR);
        panneauBas.setBorder(new EmptyBorder(10, 0, 0, 0));
        panneauBas.add(btnRetour);

        // ── Actions des boutons ────────────────────────────────────────────────
        btnActualiser.addActionListener(e -> chargerDonnees(service, parent));

        btnBloquer.addActionListener(e -> {
            int ligneSelectionnee = table.getSelectedRow();
            if (ligneSelectionnee < 0) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner un compte dans le tableau.",
                        "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Compte compte = comptesCharges.get(ligneSelectionnee);
            boolean nouvelEtat = !compte.isActif();
            String action = nouvelEtat ? "débloquer" : "bloquer";

            int confirmation = JOptionPane.showConfirmDialog(this,
                    String.format("Voulez-vous vraiment %s le compte %s (%s) ?",
                            action, compte.getNumero(), compte.getTitulaire()),
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    service.modifierStatutCompte(compte.getNumero(), nouvelEtat);
                    JOptionPane.showMessageDialog(this,
                            "Compte " + (nouvelEtat ? "débloqué" : "bloqué") + " avec succès.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees(service, parent);
                } catch (MobileMoneyException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_ACCUEIL));

        add(entete, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panneauBas, BorderLayout.SOUTH);

        chargerDonnees(service, parent);
    }

    private void chargerDonnees(ICompteService service, MainFrame parent) {
        modeleTable.setRowCount(0);
        comptesCharges = service.listerTousLesComptes();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Compte c : comptesCharges) {
            modeleTable.addRow(new Object[]{
                    c.getNumero(),
                    c.getTitulaire(),
                    String.format("%,.0f FCFA", c.getSolde()),
                    c.getDateCreation().format(fmt),
                    c.isActif() ? "✓ Actif" : "✗ Bloqué"
            });
        }

        labelNbComptes.setText(comptesCharges.size() + " compte(s) enregistré(s)");
        parent.definirStatut(comptesCharges.size() + " compte(s) chargé(s).");
    }

    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setFont(Theme.POLICE_BOUTON);
        btn.setBackground(couleur);
        btn.setForeground(Theme.BLANC);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Renderer qui colore les lignes selon le statut du compte :
     * vert clair = actif, rouge clair = bloqué.
     */
    private static class RendererStatutCompte extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(column == 2 || column == 4
                    ? CENTER : LEFT);

            if (!isSelected) {
                String statut = String.valueOf(table.getValueAt(row, 4));
                c.setBackground(statut.contains("Actif")
                        ? new Color(232, 247, 236)
                        : new Color(252, 235, 235));
            }
            return c;
        }
    }
}
