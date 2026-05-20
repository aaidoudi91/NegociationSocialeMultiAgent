package app.raison;

/** Représente une option de décision du projet rAIson (accept_offer, counter_propose_all, etc.).
 *  Le champ id est envoyé dans le POST, le champ label est rempli par Gson à la désérialisation
 *  de la réponse et sert à récupérer le nom de la décision retournée par l'API. */
public class RaisonOption {
    private String id;
    private String label;

    public RaisonOption(String id) {
        this.id = id;
    }

    public String getId() { 
        return id; 
    }
    
    public String getLabel() { 
        return label; 
    }  
}
