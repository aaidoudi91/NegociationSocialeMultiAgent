package app.agents;

import app.behaviours.NegotiationBehaviour;
import app.knowledge.SyndicatKB;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/** Agent représentant le Syndicat des travailleurs dont l'objectif est de rotéger les emplois et maximiser les
 * garanties pour les travailleurs. Il est récepteur de la négociation. */
public class SyndicatAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("[SYNDICAT] Agent démarré : " + getAID().getName());
        registerToDF(); // S'enregistre pour que la Direction puisse le trouver
        // Lance le comportement de négociation avec sa propre KB
        addBehaviour(new NegotiationBehaviour(this, new SyndicatKB(), null, false));
    }

    // Publie le service "negociation" dans le Directory Facilitator.
    private void registerToDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("negociation");
        sd.setName("SyndicatNegociateur");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println("[SYNDICAT] Enregistré dans le DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        // Désinscrire du DF à la fermeture de l'agent
        try {
            DFService.deregister(this);
            System.out.println("[SYNDICAT] Désenregistré du DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
