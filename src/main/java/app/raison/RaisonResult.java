package app.raison;

import java.util.List;

/** Représente un élément de la réponse JSON retournée par l'API rAIson : contient l'option évaluée, un booléen 
 * indiquant si elle constitue une solution valide dans le scénario courant, et une explication
 * textuelle du raisonnement ayant conduit à cette conclusion. */
public class RaisonResult {
    private RaisonOption option;
    private boolean isSolution;
    private List<String> explanation;

    public RaisonOption getOption() { return option; }
    public boolean isSolution() { return isSolution; }
    public List<String> getExplanation() { return explanation; }
}
