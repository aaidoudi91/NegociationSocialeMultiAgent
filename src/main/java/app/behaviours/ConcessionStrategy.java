package app.behaviours;

import app.model.Dimension;
import app.model.KnowledgeBase;
import app.model.Offer;

/** Calcule les nouvelles offres d'un agent en appliquant une concession partielle vers la position adverse.
 *  Deux modes sont disponibles : uniforme (rapprochement à 30% sur toutes les dimensions) et ciblé
 *  (40% sur la dimension identifiée par RAISON, 10% sur les autres). */
public class ConcessionStrategy {
    private static final double TAUX = 0.30; // Taux de concession fixe, l'agent fait un pas de 30% vers l'offre adverse

    // Calcule une nouvelle offre en rapprochant chaque dimension de l'offre adverse
    public static Offer conceder(Offer locale, Offer adverse, KnowledgeBase kb) {
        Offer min = kb.getOffreMinAcceptable();
        return new Offer.Builder()
                .postesSupprimes(ajuster(locale.getPostesSupprimes(), adverse.getPostesSupprimes(), TAUX))
                .dureeRequalification(ajuster(locale.getDureeRequalification(), adverse.getDureeRequalification(), TAUX))
                .compensationMois(ajuster(locale.getCompensationMois(), adverse.getCompensationMois(), TAUX))
                .rythmeDeploiement(ajuster(locale.getRythmeDeploiement(), adverse.getRythmeDeploiement(), TAUX))
                .prioriteRecrutement(concederBool(locale.isPrioriteRecrutement(),
                        adverse.isPrioriteRecrutement(), min.isPrioriteRecrutement()))
                .comiteSuivi(concederBool(locale.isComiteSuivi(),
                        adverse.isComiteSuivi(), min.isComiteSuivi()))
                .build();
    }

    // Concède fortement sur une dimension ciblée et légèrement sur les autres
    public static Offer concederSur(Offer locale, Offer adverse, Dimension cible, double tauxFort, KnowledgeBase kb) {
        double tauxFaible = 0.1; // Mouvement minimal sur les autres dimensions
        Offer min = kb.getOffreMinAcceptable();
        return new Offer.Builder()
                .postesSupprimes(ajuster(locale.getPostesSupprimes(), adverse.getPostesSupprimes(),
                        cible == Dimension.POSTES_SUPPRIMES ? tauxFort : tauxFaible))
                .dureeRequalification(ajuster(locale.getDureeRequalification(), adverse.getDureeRequalification(),
                        cible == Dimension.DUREE_REQUALIFICATION ? tauxFort : tauxFaible))
                .compensationMois(ajuster(locale.getCompensationMois(), adverse.getCompensationMois(),
                        cible == Dimension.COMPENSATION ? tauxFort : tauxFaible))
                .rythmeDeploiement(ajuster(locale.getRythmeDeploiement(), adverse.getRythmeDeploiement(),
                        cible == Dimension.RYTHME_DEPLOIEMENT ? tauxFort : tauxFaible))
                .prioriteRecrutement(concederBool(locale.isPrioriteRecrutement(), adverse.isPrioriteRecrutement(), 
                        min.isPrioriteRecrutement()))
                .comiteSuivi(concederBool(locale.isComiteSuivi(), adverse.isComiteSuivi(), min.isComiteSuivi()))
                .build();
    }

    // Rapproche une valeur entière de la valeur cible selon le TAUX
    private static int ajuster(int locale, int adverse, double taux) {
        int delta = (int) Math.round((adverse - locale) * taux);
        // Si la différence est trop petite et que l'arrondi donne 0, alors force un mouvement de 1 ou -1
        if (delta == 0 && adverse != locale) delta = (adverse > locale) ? 1 : -1;
        return locale + delta;
    }

    // Concession booléenne respectant les lignes rouges du KnowledgeBase.
    private static boolean concederBool(boolean locale, boolean adverse, boolean minAcceptable) {
        return minAcceptable || adverse;
    }
}
