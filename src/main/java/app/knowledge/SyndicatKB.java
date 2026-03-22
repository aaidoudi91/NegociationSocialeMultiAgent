package app.knowledge;

import app.model.*;

/** Base de Connaissances de l'Agent Syndicat représentant la stratégie de protection sociale (limiter la perte
 * d'emplois, maximiser les garanties de reclassement et les compensations financières). */
public class SyndicatKB implements KnowledgeBase {

    // Définit la position de départ idéale du Syndicat.
    @Override
    public Offer getInitialOffer() {
        return new Offer.Builder()
                .postesSupprimes(0) // Refus initial de suppression
                .dureeRequalification(24) // Demande de formation longue
                .compensationMois(18) // Demande d'indemnité maximale
                .rythmeDeploiement(36) // Étale le déploiement sur 3 ans
                .prioriteRecrutement(true) // Exige la priorité d'embauche
                .comiteSuivi(true) // Exige un droit de regard
                .build();
    }

    // Définit la pire offre acceptable pour le Syndicat avant d'appeler à la grève.
    @Override
    public Offer getMinAcceptableOffer() {
        return new Offer.Builder()
                .postesSupprimes(40)
                .dureeRequalification(12)
                .compensationMois(6)
                .rythmeDeploiement(18)
                .prioriteRecrutement(true) // Doit rester vrai
                .comiteSuivi(false) // Prêt à l'abandonner en dernier recours
                .build();
    }

    // Évalue si une offre reçue de la Direction respecte les lignes rouges syndicales.
    @Override
    public boolean isAcceptable(Offer o) {
        Offer min = getMinAcceptableOffer();
        return o.getPostesSupprimes() <= min.getPostesSupprimes()
                && o.getDureeRequalification() >= min.getDureeRequalification()
                && o.getCompensationMois() >= min.getCompensationMois()
                && o.getRythmeDeploiement() >= min.getRythmeDeploiement()
                && o.isPrioriteRecrutement();
    }


    // Bride les concessions du Syndicat pour s'assurer qu'il ne propose jamais une offre inférieure à ses propres
    // lignes rouges.
    @Override
    public Offer clamp(Offer o) {
        Offer min = getMinAcceptableOffer();
        return new Offer.Builder()
                .postesSupprimes(Math.min(o.getPostesSupprimes(), min.getPostesSupprimes()))
                .dureeRequalification(Math.max(o.getDureeRequalification(), min.getDureeRequalification()))
                .compensationMois(Math.max(o.getCompensationMois(), min.getCompensationMois()))
                .rythmeDeploiement(Math.max(o.getRythmeDeploiement(), min.getRythmeDeploiement()))
                .prioriteRecrutement(min.isPrioriteRecrutement())
                .comiteSuivi(o.isComiteSuivi())
                .build();
    }


    // Simule le moteur rAIson et génère des arguments préconçus pour défendre la position du Syndicat,
    // basés sur des données sociales et des précédents.
    @Override
    public Argument generateArgumentFor(Dimension dim) {
        return new Argument("ARG_SYND_01",
                "La requalification à 6 mois est insuffisante pour ce profil",
                "67% des travailleurs ont +45 ans. Taux de reconversion réelle à 6 mois : 23%",
                "Un plan qui échoue pour 77% des concernés ne constitue pas une protection réelle",
                Dimension.DUREE_REQUALIFICATION, Argument.Type.SUPPORT, 0.82);
    }

    // Tente de contrer un argument de la Direction en attaquant ses prémisses
    @Override
    public Argument generateAttackAgainst(Argument incoming) {
        if ("ARG_DIR_ATTACK_01".equals(incoming.getId())) {
            return new Argument("ARG_SYND_ATK_02",
                    "La certification ne garantit pas le résultat concret sur ce profil",
                    "Aucune donnée de suivi post-formation fournie pour des profils +45 ans",
                    "Sans données réelles sur ce profil, la certification est insuffisante",
                    incoming.getTargetDimension(), Argument.Type.ATTACK, 0.78
            );
        }
        return null;
    }
}
