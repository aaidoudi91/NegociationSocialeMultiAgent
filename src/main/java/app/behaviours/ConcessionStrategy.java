package app.behaviours;

import app.model.Dimension;
import app.model.Offer;

// Stratégie de concession
public class ConcessionStrategy {
    // Taux de concession fixe : l'agent fait un pas de 25% vers l'offre adverse à chaque tour
    private static final double TAUX = 0.25;

    // Calcule une nouvelle offre en rapprochant chaque dimension de l'offre adverse
    public static Offer conceder(Offer locale, Offer adverse) {
        return new Offer.Builder()
                .postesSupprimes(ajuster(locale.getPostesSupprimes(), adverse.getPostesSupprimes(), TAUX))
                .dureeRequalification(ajuster(locale.getDureeRequalification(), adverse.getDureeRequalification(), TAUX))
                .compensationMois(ajuster(locale.getCompensationMois(), adverse.getCompensationMois(), TAUX))
                .rythmeDeploiement(ajuster(locale.getRythmeDeploiement(), adverse.getRythmeDeploiement(), TAUX))
                // Les booléens sont concédés directement selon la position adverse
                .prioriteRecrutement(adverse.isPrioriteRecrutement())
                .comiteSuivi(adverse.isComiteSuivi())
                .build();
    }

    // Concède fortement sur une dimension ciblée et légèrement sur les autres
    public static Offer concederSur(Offer locale, Offer adverse, Dimension cible, double tauxFort) {
        double tauxFaible = 0.05; // movement minimal sur les autres dims
        return new Offer.Builder()
                .postesSupprimes(ajuster(locale.getPostesSupprimes(),
                        adverse.getPostesSupprimes(),
                        cible == Dimension.POSTES_SUPPRIMES ? tauxFort : tauxFaible))
                .dureeRequalification(ajuster(locale.getDureeRequalification(),
                        adverse.getDureeRequalification(),
                        cible == Dimension.DUREE_REQUALIFICATION ? tauxFort : tauxFaible))
                .compensationMois(ajuster(locale.getCompensationMois(),
                        adverse.getCompensationMois(),
                        cible == Dimension.COMPENSATION ? tauxFort : tauxFaible))
                .rythmeDeploiement(ajuster(locale.getRythmeDeploiement(),
                        adverse.getRythmeDeploiement(),
                        cible == Dimension.RYTHME_DEPLOIEMENT ? tauxFort : tauxFaible))
                .prioriteRecrutement(cible == Dimension.PRIORITE_RECRUTEMENT
                        ? adverse.isPrioriteRecrutement() : locale.isPrioriteRecrutement())
                .comiteSuivi(cible == Dimension.COMITE_SUIVI
                        ? adverse.isComiteSuivi() : locale.isComiteSuivi())
                .build();
    }

    // Rapproche une valeur entière de la valeur cible selon le TAUX
    private static int ajuster(int locale, int adverse, double taux) {
        int delta = (int) Math.round((adverse - locale) * taux);
        // Si la différence est trop petite et que l'arrondi donne 0, alors force un mouvement de 1 ou -1
        if (delta == 0 && adverse != locale) delta = (adverse > locale) ? 1 : -1;
        return locale + delta;
    }
}
