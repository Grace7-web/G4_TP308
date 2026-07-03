package cm.mobilemoney.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitaire de hachage à sens unique (SHA-256).
 *
 * Utilisé pour ne JAMAIS stocker les mots de passe et réponses secrètes
 * en clair dans la base de données — uniquement leur empreinte SHA-256.
 * L'authentification se fait en comparant deux empreintes, jamais deux
 * chaînes en clair.
 *
 * @author Équipe Core & Métier — Projet 4
 */
public final class HashUtil {

    private HashUtil() {
        // Classe utilitaire : pas d'instanciation
    }

    /**
     * Calcule l'empreinte SHA-256 d'une chaîne de caractères, encodée
     * en hexadécimal minuscule (64 caractères).
     *
     * @param texte Texte en clair (ex: mot de passe saisi)
     * @return Empreinte SHA-256 en hexadécimal
     */
    public static String sha256(String texte) {
        if (texte == null) {
            throw new IllegalArgumentException("Le texte à hacher ne peut pas être null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] empreinte = digest.digest(texte.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexa = new StringBuilder(empreinte.length * 2);
            for (byte b : empreinte) {
                hexa.append(String.format("%02x", b));
            }
            return hexa.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti disponible sur toute JVM standard
            throw new IllegalStateException("Algorithme SHA-256 indisponible.", e);
        }
    }
}
