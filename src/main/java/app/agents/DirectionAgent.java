package app.agents;

import app.behaviours.NegotiationBehaviour;
import app.knowledge.DirectionKB;
import app.raison.RaisonClient;
import app.raison.RaisonOption;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.List;

/** Agent représentant la Direction de l'entreprise, dont l'objectif est de maximiser le ROI tout en minimisant les
 *  concessions. Il est initiateur de la négociation. */
public class DirectionAgent extends Agent {
    private static final String API_KEY = "";
    private static final String APP_ID = "PRJ31675"; // Projet rAIson de la Direction

    @Override
    protected void setup() {
        System.out.println("[DIRECTION] Agent démarré : " + getAID().getName());
        // Comportement cyclique pour chercher l'agent Syndicat
        // Permet à la Direction de démarrer même si le Syndicat n'est pas encore prêt
        addBehaviour(new TickerBehaviour(this, 500) {
            @Override
            protected void onTick() {
                AID partnerAID = searchDF();
                if (partnerAID != null) {
                    System.out.println("[DIRECTION] Syndicat trouvé : " + partnerAID.getLocalName());
                    List<RaisonOption> directionOptions = List.of(
                            new RaisonOption("OPT409318"), // reject_offer
                            new RaisonOption("OPT409268"), // counter_propose_jobs
                            new RaisonOption("OPT409218"), // counter_propose_budget
                            new RaisonOption("OPT409168"), // counter_propose_timeline
                            new RaisonOption("OPT409118")  // accept_offer
                    );
                    RaisonClient raisonClient = new RaisonClient(API_KEY, APP_ID, directionOptions);
                    // raisonClient.printMetadata();
                    // Initialise la négociation avec sa KB, l'AID du partenaire et son instance RAISON
                    addBehaviour(new NegotiationBehaviour(myAgent, new DirectionKB(), partnerAID, 
                            true, raisonClient));
                    stop();
                } else {
                    System.out.println("[DIRECTION] Syndicat introuvable, nouvelle tentative...");
                }
            }
        });
    }

    // Interroge le Directory Facilitator pour trouver un agent proposant le service de négociation.
    private AID searchDF() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("negociation");
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) return results[0].getName();
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void takeDown() {
        System.out.println("[DIRECTION] Agent arrêté.");
    }
}
