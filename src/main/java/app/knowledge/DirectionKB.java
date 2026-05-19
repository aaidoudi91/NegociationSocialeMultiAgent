package app.knowledge;

import app.model.*;

/** Base de Connaissances de l'Agent Direction représentant la stratégie de l'entreprise (maximiser le ROI tout en
 * limitant le coût des mesures sociales). */
public class DirectionKB implements KnowledgeBase {

    // Définit la position de départ idéale de la Direction.
    @Override
    public Offer getOffreInitiale() {
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
    public Offer getOffreMinAcceptable() {
        return new Offer.Builder()
                .postesSupprimes(35) // Limite de rentabilité
                .dureeRequalification(15) // Concession max sur la formation
                .compensationMois(9) // Concession max sur le budget
                .rythmeDeploiement(20) // Délai max toléré
                .prioriteRecrutement(true) // Accepté comme concession finale
                .comiteSuivi(false) // Refus catégorique
                .build();
    }

    // Évalue si une offre reçue du Syndicat respecte toutes les lignes rouges de la Direction.
    @Override
    public boolean estAcceptable(Offer o) {
        Offer min = getOffreMinAcceptable();
        return o.getPostesSupprimes() >= min.getPostesSupprimes()
                && o.getDureeRequalification() <= min.getDureeRequalification()
                && o.getCompensationMois() <= min.getCompensationMois()
                && o.getRythmeDeploiement() <= min.getRythmeDeploiement();
    }

    // Force une offre à respecter les limites strictes de la Direction. Utilisé pour s'assurer que les concessions
    // générées mathématiquement ne dépassent jamais le seuil de tolérance de l'entreprise.
    @Override
    public Offer brider(Offer o) {
        Offer min = getOffreMinAcceptable();
        return new Offer.Builder()
                .postesSupprimes(Math.max(o.getPostesSupprimes(), min.getPostesSupprimes()))
                .dureeRequalification(Math.min(o.getDureeRequalification(), min.getDureeRequalification()))
                .compensationMois(Math.min(o.getCompensationMois(), min.getCompensationMois()))
                .rythmeDeploiement(Math.min(o.getRythmeDeploiement(), min.getRythmeDeploiement()))
                .prioriteRecrutement(o.isPrioriteRecrutement())
                .comiteSuivi(min.isComiteSuivi())
                .build();
    }
}
