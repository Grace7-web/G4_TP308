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
import java.util.concurrent.ExecutionException;

public class HistoriquePanel extends JPanel {

    private static final String[] COLONNES = {
            "Date / Heure", "Type", "Source", "Destination", "Montant (FCFA)", "Commission", "Statut"
    };

    private final DefaultTableModel modeleTable;
    private final JTable table;
    private final JLabel labelTitre;
    private final JButton btnActualiser;
    private final JProgressBar progressBar; // NOUVEAU : indique le chargement en cours

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

        btnActualiser = new JButton("⟳ Actualiser");
        btnActualiser.setFont(Theme.POLICE_SOUS_TITRE);
        btnActualiser.setFocusPainted(false);
        btnActualiser.setBackground(Theme.BLEU_MOYEN);
        btnActualiser.setForeground(Theme.BLANC);
        btnActualiser.setBorderPainted(false);
        btnActualiser.setOpaque(true);
        btnActualiser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // NOUVEAU : barre de progression, cachée par défaut
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(140, 18));
        progressBar.setStringPainted(true);
        progressBar.setString("Chargement...");

        JPanel entetesDroite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        entetesDroite.setBackground(Theme.FOND_CLAIR);
        entetesDroite.add(progressBar);
        entetesDroite.add(btnActualiser);

        entete.add(labelTitre, BorderLayout.WEST);
        entete.add(entetesDroite, BorderLayout.EAST);

        // ── Table ──────────────────────────────────────────────────────────────
        modeleTable = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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
     * Déclenche le chargement ASYNCHRONE des transactions via SwingWorker.
     * L'EDT reste libre pendant que la requête JDBC s'exécute en arrière-plan.
     */
    private void chargerDonnees(ICompteService service, MainFrame parent) {
        Compte compteConnecte = parent.getCompteConnecte();

        // Verrouiller l'UI le temps du chargement
        btnActualiser.setEnabled(false);
        progressBar.setVisible(true);
        parent.definirStatut("Chargement de l'historique en cours...");

        if (compteConnecte != null) {
            labelTitre.setText("Historique — Compte " + compteConnecte.getNumero());
        } else {
            labelTitre.setText("Historique — Toutes les transactions");
        }

        new HistoriqueWorker(service, compteConnecte, parent).execute();
    }

    /**
     * SwingWorker chargé d'appeler le service métier (donc potentiellement JDBC)
     * en dehors de l'EDT, puis de mettre à jour la JTable une fois terminé.
     *
     * Type générique 1 : List<Transaction>  → résultat final
     * Type générique 2 : Void               → pas de progression intermédiaire
     *                     (le service renvoie la liste complète en un bloc,
     *                      pas ligne par ligne, donc publish() n'a pas d'usage ici)
     */
    private class HistoriqueWorker extends SwingWorker<List<Transaction>, Void> {

        private final ICompteService service;
        private final Compte compteConnecte;
        private final MainFrame parent;

        HistoriqueWorker(ICompteService service, Compte compteConnecte, MainFrame parent) {
            this.service = service;
            this.compteConnecte = compteConnecte;
            this.parent = parent;
        }

        // ── S'exécute dans un thread séparé : JAMAIS toucher un composant Swing ici ──
        @Override
        protected List<Transaction> doInBackground() throws MobileMoneyException {
            if (compteConnecte != null) {
                return service.obtenirHistorique(compteConnecte.getNumero());
            } else {
                return service.obtenirToutesLesTransactions();
            }
        }

        // ── S'exécute dans l'EDT une fois doInBackground() terminée ──
        @Override
        protected void done() {
            btnActualiser.setEnabled(true);
            progressBar.setVisible(false);

            try {
                List<Transaction> transactions = get(); // relance l'exception si erreur
                remplirTable(transactions);

                if (transactions.isEmpty()) {
                    parent.definirStatut("Aucune transaction trouvée.");
                } else {
                    parent.definirStatut(transactions.size() + " transaction(s) chargée(s).");
                }
            } catch (ExecutionException ex) {
                // La cause réelle est l'exception levée dans doInBackground()
                String message = (ex.getCause() != null)
                        ? ex.getCause().getMessage()
                        : ex.getMessage();
                JOptionPane.showMessageDialog(HistoriquePanel.this, message,
                        "Erreur de chargement", JOptionPane.ERROR_MESSAGE);
                parent.definirStatut("Échec du chargement de l'historique.");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Remplit la JTable à partir d'une liste de transactions (appelée dans l'EDT uniquement). */
    private void remplirTable(List<Transaction> transactions) {
        modeleTable.setRowCount(0);
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
    }

    private static class RendererLigneColoree extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(column == 4 || column == 5 ? RIGHT : LEFT);

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