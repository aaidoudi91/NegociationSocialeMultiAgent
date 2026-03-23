package app.behaviours;

import app.model.Offer;

// Stratégie de concession : pour l'instant, concession purement mathématique puis évoluera via rAIson.
public class ConcessionStrategy {
    // Taux de concession fixe : l'agent fait un pas de 20% vers l'offre adverse à chaque tour
    private static final double RATE = 0.20;

    // Calcule une nouvelle offre en rapprochant chaque dimension de l'offre adverse
    public static Offer conceder(Offer locale, Offer adverse) {
        return new Offer.Builder()
                .postesSupprimes(ajuster(locale.getPostesSupprimes(), adverse.getPostesSupprimes()))
                .dureeRequalification(ajuster(locale.getDureeRequalification(), adverse.getDureeRequalification()))
                .compensationMois(ajuster(locale.getCompensationMois(), adverse.getCompensationMois()))
                .rythmeDeploiement(ajuster(locale.getRythmeDeploiement(), adverse.getRythmeDeploiement()))
                // Les booléens sont concédés directement selon la position adverse
                .prioriteRecrutement(adverse.isPrioriteRecrutement())
                .comiteSuivi(adverse.isComiteSuivi())
                .build();
    }

    // Rapproche une valeur entière de la valeur cible selon le taux RATE
    private static int ajuster(int locale, int adverse) {
        int delta = (int) Math.round((adverse - locale) * RATE);
        // Si la différence est trop petite et que l'arrondi donne 0, alors force un mouvement de 1 ou -1
        if (delta == 0 && adverse != locale) delta = (adverse > locale) ? 1 : -1;
        return locale + delta;
    }
}
