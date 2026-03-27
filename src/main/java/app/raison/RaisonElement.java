package app.raison;

/** Représente un élément actif envoyé à l'API RAISON.
 * On utilise l'ID plutôt que le label pour éviter tout problème de casse ou d'espace. */
public class RaisonElement {
    private final String id;

    public RaisonElement(String id) { this.id = id; }

    public String getId() { return id; }
}
