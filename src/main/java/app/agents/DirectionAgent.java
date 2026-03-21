package app.agents;

import app.behaviours.NegotiationBehaviour;
import app.knowledge.DirectionKB;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/** Agent représentant la Direction de l'entreprise, dont l'objectif est de maximiser le ROI tout en minimisant les
 * concessions. Il est initiateur de la négociation. */
public class DirectionAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("[DIRECTION] Agent démarré : " + getAID().getName());

        // Comportement cyclique (toutes les 500ms) pour chercher l'agent Syndicat
        // Permet à la Direction de démarrer même si le Syndicat n'est pas encore prêt
        addBehaviour(new TickerBehaviour(this, 500) {
            @Override
            protected void onTick() {
                AID partnerAID = searchDF();
                if (partnerAID != null) {
                    System.out.println("[DIRECTION] Syndicat trouvé : " + partnerAID.getLocalName());

                    // Initialise la négociation avec sa KB et l'AID du partenaire
                    addBehaviour(new NegotiationBehaviour(myAgent, new DirectionKB(), partnerAID, true));
                    stop(); // Arrête la recherche une fois le partenaire trouvé
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
