package app.knowledge;

import app.model.*;

/** Base de Connaissances de l'Agent Direction représentant la stratégie de l'entreprise. */
public class DirectionKB implements KnowledgeBase {

    // Définit la position de départ idéale de la Direction.
    @Override
    public Offer getOffreInitiale() {
        return new Offer.Builder()
                .postesSupprimes(75)
                .dureeRequalification(9)
                .compensationMois(3)
                .rythmeDeploiement(6)
                .prioriteRecrutement(false)
                .comiteSuivi(false)
                .build();
    }

    // Définit la pire offre acceptable pour la Direction avant de rompre les négociations.
    @Override
    public Offer getOffreMinAcceptable() {
        return new Offer.Builder()
                .postesSupprimes(35)
                .dureeRequalification(15)
                .compensationMois(9)
                .rythmeDeploiement(20)
                .prioriteRecrutement(true)
                .comiteSuivi(false)
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
    // ne dépassent jamais le seuil de tolérance de l'entreprise.
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
