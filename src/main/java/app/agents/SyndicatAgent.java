package app.agents;

import app.behaviours.NegotiationBehaviour;
import app.knowledge.SyndicatKB;
import app.raison.RaisonClient;
import app.raison.RaisonOption;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.List;

/** Agent représentant le Syndicat des travailleurs dont l'objectif est de protéger les emplois et maximiser les
 * garanties pour les travailleurs. Il est récepteur de la négociation. */
public class SyndicatAgent extends Agent {
    private static final String API_KEY = "";
    private static final String APP_ID = "PRJ30475"; // Projet rAIson du Syndicat

    @Override
    protected void setup() {
        System.out.println("[SYNDICAT] Agent démarré : " + getAID().getName());
        registerToDF(); // S'enregistre pour que la Direction puisse le trouver
        List<RaisonOption> syndicatOptions = List.of(
                new RaisonOption("OPT411418"), // counter_propose_all
                new RaisonOption("OPT408968"), // counter_propose_compensation
                new RaisonOption("OPT408918"), // reject_offer
                new RaisonOption("OPT408818"), // counter_propose_requalification
                new RaisonOption("OPT408768")); // accept_offer
        RaisonClient raisonClient = new RaisonClient(API_KEY, APP_ID, syndicatOptions);
        // Lance le comportement de négociation avec sa propre KB et son instance RAISON
        // Le Syndicat est récepteur : son partenaire sera découvert depuis le premier message reçu (partnerAID = null)
        addBehaviour(new NegotiationBehaviour(this, new SyndicatKB(), null, false, raisonClient));
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
