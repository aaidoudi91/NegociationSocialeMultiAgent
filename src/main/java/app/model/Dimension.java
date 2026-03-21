package app.model;

/** Énumération des six dimensions négociables, permet de s'assurer que les deux agents parlent exactement du même sujet
 * lors d'une argumentation. */
public enum Dimension {
    POSTES_SUPPRIMES,
    DUREE_REQUALIFICATION, // en mois
    COMPENSATION, // en mois de salaire
    RYTHME_DEPLOIEMENT, // en mois
    PRIORITE_RECRUTEMENT, // boolean
    COMITE_SUIVI // boolean
}
