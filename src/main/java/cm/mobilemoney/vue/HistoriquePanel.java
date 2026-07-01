package cm.mobilemoney.vue;

import cm.mobilemoney.exception.MobileMoneyException;
import cm.mobilemoney.metier.Compte;
import cm.mobilemoney.metier.Transaction;
import cm.mobilemoney.service.ICompteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Écran d'historique des transactions.
 *
 * Affiche soit l'historique du compte connecté (via
 * {@code ICompteService.obtenirHistorique()}), soit toutes les
 * transactions du système en mode invité (via
 * {@code obtenirToutesLesTransactions()}).
 *
 * La JTable utilise un renderer personnalisé qui colore chaque ligne
 * selon le type de transaction :
 *  - Vert clair pour les dépôts
 *  - Rouge clair pour les retraits
 *  - Orange clair pour les transferts
 *
 * Conforme au cours (semaine 7) : JTable avec modèle de données,
 * rafraîchissement après chargement.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public class HistoriquePanel extends JPanel {

    private static final String[] COLONNES = {
            "Date / Heure", "Type", "Source", "Destination", "Montant (FCFA)", "Commission", "Statut"
    };

    private final DefaultTableModel modeleTable;
    private final JTable table;
    private final JLabel labelTitre;

    public HistoriquePanel(MainFrame parent) {
        super(new BorderLayout());
        setBackground(Theme.FOND_CLAIR);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        ICompteService service = parent.getCompteService();

        // ── En-tête ────────────────────────────────────────────────────────────
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(Theme.FOND_CLAIR);
        entete.setBorder(new EmptyBorder(0, 0, 16, 0));

        labelTitre = new JLabel("Historique des transactions");
        labelTitre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        labelTitre.setForeground(Theme.BLEU_FONCE);

        JButton btnActualiser = new JButton("⟳ Actualiser");
        btnActualiser.setFont(Theme.POLICE_SOUS_TITRE);
        btnActualiser.setFocusPainted(false);
        btnActualiser.setBackground(Theme.BLEU_MOYEN);
        btnActualiser.setForeground(Theme.BLANC);
        btnActualiser.setBorderPainted(false);
        btnActualiser.setOpaque(true);
        btnActualiser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        entete.add(labelTitre, BorderLayout.WEST);
        entete.add(btnActualiser, BorderLayout.EAST);

        // ── Table ──────────────────────────────────────────────────────────────
        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table en lecture seule
            }
        };

        table = new JTable(modeleTable);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(220, 230, 245));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(Theme.BLEU_FONCE);
        table.getTableHeader().setForeground(Theme.BLANC);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setDefaultRenderer(Object.class, new RendererLigneColoree());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 236)));
        scrollPane.getViewport().setBackground(Theme.BLANC);

        // ── Bouton retour ──────────────────────────────────────────────────────
        JButton btnRetour = new JButton("← Retour à l'accueil");
        btnRetour.setFont(Theme.POLICE_SOUS_TITRE);
        btnRetour.setForeground(Theme.TEXTE_GRIS);
        btnRetour.setBorderPainted(false);
        btnRetour.setContentAreaFilled(false);
        btnRetour.setFocusPainted(false);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel panneauBas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panneauBas.setBackground(Theme.FOND_CLAIR);
        panneauBas.add(btnRetour);

        btnRetour.addActionListener(e -> parent.afficherCarte(MainFrame.CARTE_ACCUEIL));
        btnActualiser.addActionListener(e -> chargerDonnees(service, parent));

        add(entete, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panneauBas, BorderLayout.SOUTH);

        chargerDonnees(service, parent);
    }

    /**
     * Recharge les transactions depuis le service métier et met à jour
     * la table. Appelée à la construction et au clic sur "Actualiser".
     *
     * Suit le compte connecté : si un compte est connecté, n'affiche que
     * son historique ; sinon affiche toutes les transactions du système.
     */
    private void chargerDonnees(ICompteService service, MainFrame parent) {
        modeleTable.setRowCount(0);

        Compte compteConnecte = parent.getCompteConnecte();
        List<Transaction> transactions;

        if (compteConnecte != null) {
            labelTitre.setText("Historique — Compte " + compteConnecte.getNumero());
            try {
                transactions = service.obtenirHistorique(compteConnecte.getNumero());
            } catch (MobileMoneyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            labelTitre.setText("Historique — Toutes les transactions");
            transactions = service.obtenirToutesLesTransactions();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Transaction t : transactions) {
            modeleTable.addRow(new Object[]{
                    t.getDateHeure().format(fmt),
                    t.getType().getLibelle(),
                    t.getNumeroSource() != null ? t.getNumeroSource() : "EXTERNE",
                    t.getNumeroDestination() != null ? t.getNumeroDestination() : "EXTERNE",
                    String.format("%,.0f", t.getMontant()),
                    String.format("%,.0f", t.getCommission()),
                    t.isSucces() ? "✓ Succès" : "✗ Échec"
            });
        }

        if (transactions.isEmpty()) {
            parent.definirStatut("Aucune transaction trouvée.");
        } else {
            parent.definirStatut(transactions.size() + " transaction(s) chargée(s).");
        }
    }

    /**
     * Renderer personnalisé qui colore chaque ligne de la table selon
     * le type de transaction (colonne "Type", index 1).
     */
    private static class RendererLigneColoree extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(column == 4 || column == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT);

            if (!isSelected) {
                String type = String.valueOf(table.getValueAt(row, 1));
                Color fond = switch (type) {
                    case "Dépôt" -> new Color(232, 247, 236);
                    case "Retrait" -> new Color(252, 235, 235);
                    case "Transfert" -> new Color(255, 246, 233);
                    default -> Theme.BLANC;
                };
                c.setBackground(fond);
            }

            return c;
        }
    }
}