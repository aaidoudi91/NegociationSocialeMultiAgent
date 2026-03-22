package app.model;

import java.io.Serializable;

/** Représente une proposition (ou contre-proposition) échangée entre les agents.
 * Immuable pour garantir qu'aucune offre n'est modifiée après sa création ou pendant son transit via JADE. */
public class Offer implements Serializable {
    private final int postesSupprimes;
    private final int dureeRequalification;
    private final int compensationMois;
    private final int rythmeDeploiement;
    private final boolean prioriteRecrutement;
    private final boolean comiteSuivi;

    // Le constructeur est privé, on force l'utilisation du Builder
    private Offer(Builder b) {
        this.postesSupprimes = b.postesSupprimes;
        this.dureeRequalification = b.dureeRequalification;
        this.compensationMois = b.compensationMois;
        this.rythmeDeploiement = b.rythmeDeploiement;
        this.prioriteRecrutement = b.prioriteRecrutement;
        this.comiteSuivi = b.comiteSuivi;
    }

    // Getters uniquement, pas de Setters
    public int getPostesSupprimes() { return postesSupprimes; }
    public int getDureeRequalification() { return dureeRequalification; }
    public int getCompensationMois() { return compensationMois; }
    public int getRythmeDeploiement() { return rythmeDeploiement; }
    public boolean isPrioriteRecrutement() { return prioriteRecrutement; }
    public boolean isComiteSuivi() { return comiteSuivi; }

    @Override
    public String toString() {
        return String.format("Offer{postes=%d, requalif=%dmois, compens=%dmois, rythme=%dmois, priorite=%b, comite=%b}",
                postesSupprimes, dureeRequalification, compensationMois, rythmeDeploiement,
                prioriteRecrutement, comiteSuivi);
    }

    // Classe utilitaire permettant de construire une offre.
    public static class Builder {
        private int postesSupprimes;
        private int dureeRequalification;
        private int compensationMois;
        private int rythmeDeploiement;
        private boolean prioriteRecrutement;
        private boolean comiteSuivi;

        public Builder postesSupprimes(int v) {
            this.postesSupprimes = v;
            return this;
        }
        public Builder dureeRequalification(int v) {
            this.dureeRequalification = v;
            return this;
        }
        public Builder compensationMois(int v) {
            this.compensationMois = v;
            return this;
        }
        public Builder rythmeDeploiement(int v) {
            this.rythmeDeploiement = v;
            return this;
        }
        public Builder prioriteRecrutement(boolean v){
            this.prioriteRecrutement = v;
            return this;
        }
        public Builder comiteSuivi(boolean v) {
            this.comiteSuivi = v;
            return this;
        }

        public Offer build() { return new Offer(this); }
    }
}
