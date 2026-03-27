package app.raison;

import java.util.List;

/** Corps de la requête POST envoyée à l'API rAIson : encapsule la liste des éléments actifs détectés par l'agent
 * ainsi que le nombre de solutions souhaitées en retour. */
public class RaisonRequest {
    private List<RaisonElement> elements;
    private int limit;

    public RaisonRequest(List<RaisonElement> elements) {
        this.elements = elements;
        this.limit = 1;
    }
}
