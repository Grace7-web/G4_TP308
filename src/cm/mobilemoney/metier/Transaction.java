package cm.mobilemoney.metier;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Classe métier représentant une transaction financière enregistrée.
 * Une transaction est IMMUABLE après création (tous les attributs sont final).
 * Cela garantit l'intégrité de l'historique.
 *
 * Correspond directement à la table SQL : transactions(id, source, destination, montant, date, type, commission)
 *
 * @author Équipe Core & Métier — Projet 4
 */
public class Transaction implements Serializable, Comparable<Transaction> {

    private static final long serialVersionUID = 1L;

    // ── Énumération des types de transactions ─────────────────────────────────

    /**
     * Types de transactions supportés par le système Mobile Money.
     */
    public enum TypeTransaction {
        DEPOT("Dépôt"),
        RETRAIT("Retrait"),
        TRANSFERT("Transfert");

        private final String libelle;

        TypeTransaction(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // ── Attributs immuables ───────────────────────────────────────────────────

    /** Identifiant unique de la transaction (UUID) */
    private final String id;

    /** Type de l'opération */
    private final TypeTransaction type;

    /** Numéro du compte source (null pour un dépôt externe) */
    private final String numeroSource;

    /** Numéro du compte destination (null pour un retrait) */
    private final String numeroDestination;

    /** Montant de l'opération en FCFA (hors commission) */
    private final double montant;

    /** Commission prélevée par le réseau en FCFA */
    private final double commission;

    /** Horodatage exact de l'opération */
    private final LocalDateTime dateHeure;

    /** Statut de la transaction */
    private final boolean succes;

    // ── Constructeur ──────────────────────────────────────────────────────────

    /**
     * Crée une transaction avec toutes ses informations.
     * Génère automatiquement un ID unique et un horodatage.
     *
     * @param type               Type de la transaction
     * @param numeroSource       Numéro du compte débiteur (peut être null)
     * @param numeroDestination  Numéro du compte crédité (peut être null)
     * @param montant            Montant échangé en FCFA
     * @param commission         Frais prélevés en FCFA
     * @param succes             true si la transaction a abouti
     */
    public Transaction(TypeTransaction type,
                       String numeroSource,
                       String numeroDestination,
                       double montant,
                       double commission,
                       boolean succes) {

        if (type == null) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire.");
        }
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }
        if (commission < 0) {
            throw new IllegalArgumentException("La commission ne peut pas être négative.");
        }

        // UUID tronqué pour lisibilité en UI (8 premiers caractères)
        this.id                 = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.type               = type;
        this.numeroSource       = numeroSource;
        this.numeroDestination  = numeroDestination;
        this.montant            = montant;
        this.commission         = commission;
        this.dateHeure          = LocalDateTime.now();
        this.succes             = succes;
    }

    // ── Getters (aucun setter : la transaction est immuable) ──────────────────

    public String getId() { return id; }

    public TypeTransaction getType() { return type; }

    public String getNumeroSource() { return numeroSource; }

    public String getNumeroDestination() { return numeroDestination; }

    public double getMontant() { return montant; }

    public double getCommission() { return commission; }

    public LocalDateTime getDateHeure() { return dateHeure; }

    public boolean isSucces() { return succes; }

    /**
     * Retourne le montant total prélevé sur le compte source
     * (montant + commission), uniquement pour les débits.
     */
    public double getMontantTotal() {
        return montant + commission;
    }

    // ── Tri naturel : du plus récent au plus ancien ────────────────────────────

    @Override
    public int compareTo(Transaction autre) {
        // Ordre inversé → les plus récentes en tête de liste
        return autre.dateHeure.compareTo(this.dateHeure);
    }

    // ── toString lisible pour les logs et l'IHM ───────────────────────────────

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String src  = (numeroSource != null)      ? numeroSource      : "EXTERNE";
        String dest = (numeroDestination != null)  ? numeroDestination : "EXTERNE";

        return String.format(
                "[%s] %s | %s → %s | Montant: %.2f FCFA | Commission: %.2f FCFA | %s | %s",
                dateHeure.format(fmt),
                type.getLibelle(),
                src, dest,
                montant, commission,
                succes ? "SUCCÈS" : "ÉCHEC",
                id
        );
    }
}
