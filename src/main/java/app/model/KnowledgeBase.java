package app.model;

/** Interface représentant la base de connaissances privée d'un agent, agissant comme une façade entre le comportement
 * de négociation JADE et le système d'argumentation rAIson. */
public interface KnowledgeBase {
    // Retourne la position initiale de l'agent avant le début des concessions.
    Offer getOffreInitiale();
    // Retourne la position minimale acceptable.
    Offer getOffreMinAcceptable();
    // Vérifie si l'offre adverse satisfait l'acceptabilité de la KB.
    boolean estAcceptable(Offer offer);
    // Ajuste une offre calculée pour s'assurer qu'elle ne dépasse pas les limites fixées par getOffreMinAcceptable().
    Offer brider(Offer offer);

    // Interroge le système rAIson pour générer un argument justifiant la position de l'agent sur une dimension.
    Argument genererArgumentPour(Dimension dimension);
    // Évalue un argument reçu de l'adversaire au tour précédent et tente de formuler une attaque.
    Argument genererArgumentContre(Argument incoming);
}
