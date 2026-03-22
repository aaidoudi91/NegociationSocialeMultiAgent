package app.knowledge;

import app.model.*;

/** Base de Connaissances de l'Agent Direction représentant la stratégie de l'entreprise (maximiser le ROI tout en
 * limitant le coût des mesures sociales). */
public class DirectionKB implements KnowledgeBase {
    
    // Définit la position de départ idéale de la Direction.
    @Override
    public Offer getInitialOffer() {
        return new Offer.Builder()
                .postesSupprimes(80)
                .dureeRequalification(6) // Formation courte 
                .compensationMois(0) // Aucune compensation 
                .rythmeDeploiement(6) // Déploiement très rapide
                .prioriteRecrutement(false) // Aucune garantie d'embauche
                .comiteSuivi(false) // Pas de contrôle syndical
                .build();
    }

    // Définit la pire offre acceptable pour la Direction avant de rompre les négociations.
    @Override
    public Offer getMinAcceptableOffer() {
        return new Offer.Builder()
                .postesSupprimes(40) // Limite de rentabilité
                .dureeRequalification(15) // Concession max sur la formation
                .compensationMois(9) // Concession max sur le budget
                .rythmeDeploiement(18) // Délai max toléré
                .prioriteRecrutement(true) // Accepté comme concession finale
                .comiteSuivi(false) // Refus catégorique
                .build();
    }

    // Évalue si une offre reçue du Syndicat respecte toutes les lignes rouges de la Direction.
    @Override
    public boolean isAcceptable(Offer o) {
        Offer min = getMinAcceptableOffer();
        return o.getPostesSupprimes() >= min.getPostesSupprimes()
                && o.getDureeRequalification() <= min.getDureeRequalification()
                && o.getCompensationMois() <= min.getCompensationMois()
                && o.getRythmeDeploiement() <= min.getRythmeDeploiement();
    }

    // Force une offre à respecter les limites strictes de la Direction. Utilisé pour s'assurer que les concessions
    // générées mathématiquement ne dépassent jamais le seuil de tolérance de l'entreprise.
    @Override
    public Offer clamp(Offer o) {
        Offer min = getMinAcceptableOffer();
        return new Offer.Builder()
                .postesSupprimes(Math.max(o.getPostesSupprimes(), min.getPostesSupprimes()))
                .dureeRequalification(Math.min(o.getDureeRequalification(), min.getDureeRequalification()))
                .compensationMois(Math.min(o.getCompensationMois(), min.getCompensationMois()))
                .rythmeDeploiement(Math.min(o.getRythmeDeploiement(), min.getRythmeDeploiement()))
                .prioriteRecrutement(o.isPrioriteRecrutement())
                .comiteSuivi(min.isComiteSuivi())
                .build();
    }


    // Simule le moteur rAIson. Génère des arguments préconçus pour défendre la position de la Direction sur une
    // dimension spécifique, basés sur des données financières.
    @Override
    public Argument generateArgumentFor(Dimension dim) {
        return new Argument("ARG_DIR_RYTHME",
                "Un déploiement en 18 mois est la limite de viabilité économique",
                "ROI atteint en 18 mois selon nos projections financières internes",
                "Retarder détériore le ROI",
                Dimension.RYTHME_DEPLOIEMENT, Argument.Type.SUPPORT, 0.75);
    }

    // Tente de contrer un argument spécifique reçu du Syndicat. Retourne null si la Direction n'en a pas.
    @Override
    public Argument generateAttackAgainst(Argument incoming) {
        if ("ARG_SYND_01".equals(incoming.getId())) {
            return new Argument("ARG_DIR_ATTACK_01",
                    "Nos formations sont sur mesure, non comparables aux statistiques sectorielles",
                    "Programme de requalification validé par 3 organismes certifiés",
                    "Les statistiques générales ne s'appliquent pas à un dispositif dédié",
                    incoming.getTargetDimension(), Argument.Type.ATTACK, 0.55);
        }
        return null; // pas d'attaque connue donc on ne fait rien
    }
}
