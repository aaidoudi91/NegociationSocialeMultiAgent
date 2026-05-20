package app.model;

/** Interface représentant la base de connaissances privée d'un agent, agissant comme une façade entre le comportement
 *  de négociation JADE et le système d'argumentation rAIson. */
public interface KnowledgeBase {
    // Retourne la position initiale de l'agent avant le début des concessions.
    Offer getOffreInitiale();
    // Retourne la position limite : aucune concession ne peut franchir ces seuils.
    Offer getOffreMinAcceptable();
    // Vérifie si l'offre adverse satisfait les critères d'acceptabilité de la KB.
    boolean estAcceptable(Offer offer);
    // Plafonne chaque dimension de l'offre aux bornes de getOffreMinAcceptable(),
    // garantissant qu'un agent ne peut jamais émettre une offre hors de ses limites.
    Offer brider(Offer offer);
}
