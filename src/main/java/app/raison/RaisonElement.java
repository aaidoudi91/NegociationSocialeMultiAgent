package app.raison;

/** Représente un élément actif envoyé à l'API rAIson, identifié par son ID. */
public class RaisonElement {
    private String id;
    
    public RaisonElement(String id) { 
        this.id = id; 
    }
    
    public String getId() { 
        return id; 
    }
}
