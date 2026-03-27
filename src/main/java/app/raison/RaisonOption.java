package app.raison;

/** Représente une option de décision retournée par l'API rAIson : chaque option possède un identifiant unique et 
 * un label utilisé par le NegotiationBehaviour pour choisir l'action suivante (ex. : "accept_offer", 
 * "counter_propose_requalification", "reject_offer"). */
public class RaisonOption {
    private String id;
    private String label;

    public String getId()    { return id; }
    public String getLabel() { return label; }
}
