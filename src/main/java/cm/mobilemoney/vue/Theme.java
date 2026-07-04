package cm.mobilemoney.vue;


import java.awt.*;

/**
 * Constantes de thème graphique partagées par tous les écrans de l'application.
 * Style "fintech moderne" : bleu foncé professionnel avec accents orange/or
 * pour le dynamisme (boutons d'action, montants, mise en avant).
 *
 * Centraliser les couleurs ici évite la duplication et permet de changer
 * le thème de toute l'application en un seul endroit.
 *
 * @author Équipe IHM Swing — Projet 4
 */
public final class Theme {

    private Theme() {
        // Classe utilitaire : pas d'instanciation
    }

    // ── Palette de couleurs ────────────────────────────────────────────────────

    public static final Color BLEU_FONCE        = new Color(13, 35, 64);    // #0D2340 fond principal
    public static final Color BLEU_NUIT         = new Color(8, 22, 42);     // #08162A dégradé sombre
    public static final Color BLEU_MOYEN        = new Color(30, 70, 120);   // #1E4678
    public static final Color BLEU_CLAIR        = new Color(70, 130, 200);  // #4682C8

    public static final Color ORANGE_VIF        = new Color(245, 130, 32);  // #F58220 accent principal
    public static final Color ORANGE_FONCE      = new Color(204, 102, 17);  // hover / pressed
    public static final Color OR                = new Color(230, 184, 84); // #E6B854 montants / titres

    public static final Color FOND_CLAIR        = new Color(240, 243, 248);// #F0F3F8
    public static final Color BLANC             = Color.WHITE;
    public static final Color TEXTE_SOMBRE      = new Color(33, 37, 41);
    public static final Color TEXTE_GRIS        = new Color(120, 130, 140);
    public static final Color VERT_SUCCES       = new Color(46, 184, 92);
    public static final Color ROUGE_ERREUR      = new Color(230, 70, 70);

    // ── Polices ────────────────────────────────────────────────────────────────

    public static final Font POLICE_TITRE       = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font POLICE_SOUS_TITRE  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font POLICE_LABEL       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font POLICE_BOUTON      = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font POLICE_MONTANT     = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font POLICE_CARTE_TITRE = new Font("Segoe UI", Font.BOLD, 15);

    // ── Utilitaire : panneau avec fond en dégradé vertical ────────────────────

    /**
     * Crée un JPanel personnalisé dont le fond est un dégradé linéaire
     * entre deux couleurs (du haut vers le bas).
     */
    public static javax.swing.JPanel creerPanelDegrade(Color haut, Color bas) {
        return new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint degrade = new GradientPaint(
                        0, 0, haut, 0, getHeight(), bas);
                g2.setPaint(degrade);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
}