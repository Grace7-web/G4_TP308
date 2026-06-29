package cm.mobilemoney.metier;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe métier représentant un compte Mobile Money.
 * Encapsulation stricte : tous les attributs sont privés.
 * Aucune logique métier ici — uniquement les données et leurs règles d'intégrité.
 *
 * @author Équipe Core & Métier — Projet 4
 */
public class Compte implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Attributs privés ──────────────────────────────────────────────────────

    /** Numéro de compte unique (ex: "CM-001-2025") */
    private final String numero;

    /** Nom complet du titulaire */
    private String titulaire;

    /** Solde courant en FCFA */
    private double solde;

    /** Date de création du compte */
    private final LocalDateTime dateCreation;

    /** Indique si le compte est actif ou bloqué */
    private boolean actif;

    // ── Constantes métier ─────────────────────────────────────────────────────

    /** Solde minimum autorisé en FCFA (seuil de sécurité) */
    public static final double SOLDE_MINIMUM = 500.0;

    // ── Constructeurs ─────────────────────────────────────────────────────────

    /**
     * Constructeur complet pour la création d'un nouveau compte.
     *
     * @param numero    Numéro unique du compte
     * @param titulaire Nom complet du titulaire
     * @param soldeInitial Solde d'ouverture (doit être >= SOLDE_MINIMUM)
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public Compte(String numero, String titulaire, double soldeInitial) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Le numéro de compte ne peut pas être vide.");
        }
        if (titulaire == null || titulaire.isBlank()) {
            throw new IllegalArgumentException("Le nom du titulaire ne peut pas être vide.");
        }
        if (soldeInitial < SOLDE_MINIMUM) {
            throw new IllegalArgumentException(
                    "Le solde initial doit être au minimum de " + SOLDE_MINIMUM + " FCFA."
            );
        }

        this.numero       = numero.trim().toUpperCase();
        this.titulaire    = titulaire.trim();
        this.solde        = soldeInitial;
        this.dateCreation = LocalDateTime.now();
        this.actif        = true;
    }

    // ── Méthodes métier internes ───────────────────────────────────────────────

    /**
     * Crédite le compte d'un montant donné.
     * Méthode package-private : uniquement le service peut l'appeler.
     *
     * @param montant Montant positif en FCFA
     */
    public void crediter(double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant à créditer doit être positif.");
        }
        this.solde += montant;
    }

    /**
     * Débite le compte d'un montant donné.
     * Méthode package-private : uniquement le service peut l'appeler.
     * Ne vérifie PAS la suffisance du solde — c'est la responsabilité du service.
     *
     * @param montant Montant positif en FCFA
     */
    public void debiter(double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant à débiter doit être positif.");
        }
        this.solde -= montant;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getNumero() {
        return numero;
    }

    public String getTitulaire() {
        return titulaire;
    }

    public double getSolde() {
        return solde;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public boolean isActif() {
        return actif;
    }

    // ── Setters contrôlés ─────────────────────────────────────────────────────

    public void setTitulaire(String titulaire) {
        if (titulaire == null || titulaire.isBlank()) {
            throw new IllegalArgumentException("Le nom du titulaire ne peut pas être vide.");
        }
        this.titulaire = titulaire.trim();
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    // ── equals / hashCode basés sur le numéro unique ──────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compte)) return false;
        Compte autre = (Compte) o;
        return this.numero.equals(autre.numero);
    }

    @Override
    public int hashCode() {
        return numero.hashCode();
    }

    // ── toString pour débogage et logs ────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
                "Compte{numéro='%s', titulaire='%s', solde=%.2f FCFA, actif=%b}",
                numero, titulaire, solde, actif
        );
    }
}
