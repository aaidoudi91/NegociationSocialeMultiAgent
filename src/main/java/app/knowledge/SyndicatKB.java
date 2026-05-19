package app.knowledge;

import app.model.*;

/** Base de Connaissances de l'Agent Syndicat représentant la stratégie de protection sociale (limiter la perte
 * d'emplois, maximiser les garanties de reclassement et les compensations financières). */
public class SyndicatKB implements KnowledgeBase {

    // Définit la position de départ idéale du Syndicat.
    @Override
    public Offer getOffreInitiale() {
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
    public Offer getOffreMinAcceptable() {
        return new Offer.Builder()
                .postesSupprimes(45)
                .dureeRequalification(12)
                .compensationMois(6)
                .rythmeDeploiement(15)
                .prioriteRecrutement(true) // Doit rester vrai
                .comiteSuivi(false) // Prêt à l'abandonner en dernier recours
                .build();
    }

    // Évalue si une offre reçue de la Direction respecte les lignes rouges syndicales.
    @Override
    public boolean estAcceptable(Offer o) {
        Offer min = getOffreMinAcceptable();
        return o.getPostesSupprimes() <= min.getPostesSupprimes()
                && o.getDureeRequalification() >= min.getDureeRequalification()
                && o.getCompensationMois() >= min.getCompensationMois()
                && o.getRythmeDeploiement() >= min.getRythmeDeploiement()
                && o.isPrioriteRecrutement();
    }


    // Bride les concessions du Syndicat pour s'assurer qu'il ne propose jamais une offre inférieure à ses propres
    // lignes rouges.
    @Override
    public Offer brider(Offer o) {
        Offer min = getOffreMinAcceptable();
        return new Offer.Builder()
                .postesSupprimes(Math.min(o.getPostesSupprimes(), min.getPostesSupprimes()))
                .dureeRequalification(Math.max(o.getDureeRequalification(), min.getDureeRequalification()))
                .compensationMois(Math.max(o.getCompensationMois(), min.getCompensationMois()))
                .rythmeDeploiement(Math.max(o.getRythmeDeploiement(), min.getRythmeDeploiement()))
                .prioriteRecrutement(min.isPrioriteRecrutement())
                .comiteSuivi(o.isComiteSuivi())
                .build();
    }
}
