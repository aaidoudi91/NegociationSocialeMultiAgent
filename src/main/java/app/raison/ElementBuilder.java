package app.raison;

import app.model.KnowledgeBase;
import app.model.Offer;
import java.util.ArrayList;
import java.util.List;

/** Traduit l'état courant d'une offre Java en liste d'éléments rAIson à soumettre à l'API.
 *  forSyndicat() évalue une offre reçue de la Direction, forDirection() évalue une contre-offre reçue du Syndicat.*/
public class ElementBuilder {

    // IDs Syndicat
    private static final String S_POSTES_OK = "OPT408518"; // postes supprimés satisfaisants
    private static final String S_REQUALIF_OK = "OPT408568"; // durée de requalification satisfaisante
    private static final String S_REQUALIF_KO = "OPT408868"; // durée de requalification insuffisante
    private static final String S_COMPENS_OK = "OPT408618"; // compensation financière satisfaisante
    private static final String S_RYTHME_OK = "OPT408668"; // rythme de déploiement satisfaisant
    private static final String S_COMPENS_KO = "OPT409018"; // compensation financière insuffisante

    // IDs Direction
    private static final String D_COMPENS_OK = "OPT409418"; // compensation dans les limites acceptables
    private static final String D_RYTHME_OK = "OPT409468"; // rythme de déploiement dans les limites acceptables
    private static final String D_RYTHME_KO = "OPT409568"; // rythme de déploiement trop lent
    private static final String D_POSTES_OK = "OPT409368"; // nombre de postes supprimés satisfaisant
    private static final String D_POSTES_KO = "OPT411918"; // nombre de postes supprimés insuffisant
    private static final String D_REQUALIF_OK = "OPT409518"; // durée de requalification dans les limites acceptables
    private static final String D_COMPENS_KO = "OPT409618"; // compensation trop élevée

    // Syndicat évalue une offre reçue, seuils lus depuis SyndicatKB
    public static List<RaisonElement> forSyndicat(Offer r, KnowledgeBase kb) {
        Offer min = kb.getOffreMinAcceptable(); // source unique de vérité
        List<RaisonElement> elements = new ArrayList<>();

        if (r.getPostesSupprimes() <= min.getPostesSupprimes()) 
            elements.add(new RaisonElement(S_POSTES_OK));

        if (r.getDureeRequalification() >= min.getDureeRequalification())
            elements.add(new RaisonElement(S_REQUALIF_OK));
        else 
            elements.add(new RaisonElement(S_REQUALIF_KO));

        if (r.getCompensationMois() >= min.getCompensationMois()) 
            elements.add(new RaisonElement(S_COMPENS_OK));
        else 
            elements.add(new RaisonElement(S_COMPENS_KO));

        if (r.getRythmeDeploiement() >= min.getRythmeDeploiement()) 
            elements.add(new RaisonElement(S_RYTHME_OK));

        return elements;
    }

    // Direction évalue une contre-offre reçue, seuils lus depuis DirectionKB
    public static List<RaisonElement> forDirection(Offer r, KnowledgeBase kb) {
        Offer min = kb.getOffreMinAcceptable(); // source unique de vérité
        List<RaisonElement> elements = new ArrayList<>();

        if (r.getPostesSupprimes() >= min.getPostesSupprimes())
            elements.add(new RaisonElement(D_POSTES_OK));
        else
            elements.add(new RaisonElement(D_POSTES_KO));

        if (r.getCompensationMois() <= min.getCompensationMois())
            elements.add(new RaisonElement(D_COMPENS_OK));
        else
            elements.add(new RaisonElement(D_COMPENS_KO));

        if (r.getRythmeDeploiement() <= min.getRythmeDeploiement())
            elements.add(new RaisonElement(D_RYTHME_OK));
        else
            elements.add(new RaisonElement(D_RYTHME_KO));

        if (r.getDureeRequalification() <= min.getDureeRequalification())
            elements.add(new RaisonElement(D_REQUALIF_OK));

        return elements;
    }
}
