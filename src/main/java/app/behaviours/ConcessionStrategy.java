package app.behaviours;

import app.model.Offer;

// Stratégie de concession : pour l'instant, concession purement mathématique puis évoluera via rAIson.
public class ConcessionStrategy {
    // Taux de concession fixe : l'agent fait un pas de 20% vers l'offre adverse à chaque tour
    private static final double RATE = 0.20;

    // Calcule une nouvelle offre en rapprochant chaque dimension de l'offre adverse
    public static Offer concede(Offer mine, Offer theirs) {
        return new Offer.Builder()
                .postesSupprimes(move(mine.getPostesSupprimes(), theirs.getPostesSupprimes()))
                .dureeRequalification(move(mine.getDureeRequalification(), theirs.getDureeRequalification()))
                .compensationMois(move(mine.getCompensationMois(), theirs.getCompensationMois()))
                .rythmeDeploiement(move(mine.getRythmeDeploiement(), theirs.getRythmeDeploiement()))
                // Les booléens sont concédés directement selon la position adverse
                .prioriteRecrutement(theirs.isPrioriteRecrutement())
                .comiteSuivi(theirs.isComiteSuivi())
                .build();
    }

    // Rapproche une valeur entière de la valeur cible selon le taux RATE
    private static int move(int mine, int theirs) {
        int delta = (int) Math.round((theirs - mine) * RATE);
        // Si la différence est trop petite et que l'arrondi donne 0, alors force un mouvement de 1 ou -1
        if (delta == 0 && theirs != mine) delta = (theirs > mine) ? 1 : -1;
        return mine + delta;
    }
}
