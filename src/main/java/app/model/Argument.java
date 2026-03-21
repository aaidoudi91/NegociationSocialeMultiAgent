package app.model;

import java.io.Serializable;

/** Modèle d'un argument échangé lors de la négociation. Immuable pour sécuriser les échanges via JADE. Plus tard, ces
 * valeurs seront générées dynamiquement par le rAIson.*/
public class Argument implements Serializable {
    public enum Type { SUPPORT, ATTACK }

    private final String id;
    private final String claim; // L'affirmation (ex: "La requalification est insuffisante")
    private final String evidence; // La donnée brute (ex: "Profil métier complexe")
    private final String warrant; // Le lien logique (ex: "Un métier complexe requiert plus de temps")
    private final Dimension targetDimension; // La dimension visée par cet argument
    private final Type type; // SUPPORT (pour soi) ou ATTACK (contre l'autre)
    private final double strength; // Force de l'argument (0.0 à 1.0)

    public Argument(String id, String claim, String evidence, String warrant,
                    Dimension targetDimension, Type type, double strength) {
        this.id = id;
        this.claim = claim;
        this.evidence = evidence;
        this.warrant = warrant;
        this.targetDimension = targetDimension;
        this.type = type;
        this.strength = strength;
    }

    // Getters pour l'accès en lecture
    public String getId() { return id; }
    public String getClaim() { return claim; }
    public String getEvidence() { return evidence; }
    public String getWarrant() { return warrant; }
    public Dimension getTargetDimension() { return targetDimension; }
    public Type getType() { return type; }
    public double getStrength() { return strength; }

    @Override
    public String toString() {
        return String.format("[%s | force=%.2f] %s\n  Evidence: %s\n  Warrant: %s",
                id, strength, claim, evidence, warrant);
    }
}
