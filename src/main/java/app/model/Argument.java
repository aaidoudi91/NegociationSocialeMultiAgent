package app.model;

import java.io.Serializable;

/** Modèle d'un argument échangé lors de la négociation. Immuable pour sécuriser les échanges via JADE. Plus tard, ces
 * valeurs seront générées dynamiquement par le rAIson.*/
public class Argument implements Serializable {
    public enum Type {SUPPORT, ATTAQUE}

    private final String id;
    private final String affirmation; // (ex: "la requalification est insuffisante")
    private final String preuve; // (ex: "profil métier complexe")
    private final String justification; // (ex: "un métier complexe requiert plus de temps")
    private final Dimension dimensionCible;
    private final Type type; // SUPPORT (pour soi) ou ATTAQUE (contre l'autre)
    private final double force; // de 0,0 à 1,0

    public Argument(String id, String affirmation, String preuve, String justification, Dimension dimensionCible,
                    Type type, double force) {
        this.id = id;
        this.affirmation = affirmation;
        this.preuve = preuve;
        this.justification = justification;
        this.dimensionCible = dimensionCible;
        this.type = type;
        this.force = force;
    }

    // Getters pour l'accès en lecture
    public String getId() { return id; }
    public String getAffirmation() { return affirmation; }
    public String getPreuve() { return preuve; }
    public String getJustification() { return justification; }
    public Dimension getDimensionCible() { return dimensionCible; }
    public Type getType() { return type; }
    public double getForce() { return force; }

    @Override
    public String toString() {
        return String.format("[%s | force=%.2f] %s\n  Preuve: %s\n  Justification: %s", id, force, affirmation,
                preuve, justification);
    }
}
