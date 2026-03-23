package app;

import app.knowledge.DirectionKB;
import app.knowledge.SyndicatKB;
import app.model.Argument;
import app.model.Dimension;
import app.model.KnowledgeBase;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "false");
        AgentContainer container = rt.createMainContainer(p);

        container.createNewAgent("Direction", "app.agents.DirectionAgent", null).start();
        container.createNewAgent("Syndicat", "app.agents.SyndicatAgent", null).start();

        // Test des arguments
        KnowledgeBase directionKB = new DirectionKB();
        KnowledgeBase syndicatKB  = new SyndicatKB();
        // Syndicat attaque la dimension DUREE_REQUALIFICATION
        Argument argSynd = syndicatKB.genererArgumentPour(Dimension.DUREE_REQUALIFICATION);
        System.out.println("Argument Syndicat : " + argSynd);
        // Direction tente de l'attaquer
        Argument attack = directionKB.genererArgumentContre(argSynd);
        System.out.println("Attaque Direction : " + (attack != null ? attack : "Aucune attaque disponible"));
        // Syndicat contre-attaque
        Argument counterAtk = syndicatKB.genererArgumentContre(attack);
        System.out.println("Contre-attaque Syndicat : " + (counterAtk != null ? counterAtk : "Aucune contre-attaque"));

        System.out.println("\nOffre Direction : " + directionKB.getOffreInitiale());
        System.out.println("Offre Syndicat : " + syndicatKB.getOffreInitiale());
    }
}
