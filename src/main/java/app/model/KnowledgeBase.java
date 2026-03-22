package app.model;

/** Interface représentant la base de connaissances privée d'un agent, agissant comme une façade entre le comportement
 * de négociation JADE et le système d'argumentation rAIson. */
public interface KnowledgeBase {
    // Retourne la position initiale de l'agent avant le début des concessions.
    Offer getInitialOffer();

    // Retourne la position minimale acceptable.
    Offer getMinAcceptableOffer();

    // Vérifie si l'offre adverse satisfait l'acceptabilité de la KB.
    boolean isAcceptable(Offer offer);

    // Ajuste une offre calculée pour s'assurer qu'elle ne dépasse pas les limites fixées par getMinAcceptableOffer().
    Offer clamp(Offer offer);

    // Interroge le système rAIson pour générer un argument justifiant la position de l'agent sur une dimension.
    Argument generateArgumentFor(Dimension dimension);

    // Évalue un argument reçu de l'adversaire au tour précédent et tente de formuler une attaque.
    Argument generateAttackAgainst(Argument incoming);
}
