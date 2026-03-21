package app.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import app.model.KnowledgeBase;
import app.model.Offer;

/** Comportement JADE gérant la boucle de négociation multi-tours. Implémenté en tant que CyclicBehaviour pour écouter
 * et répondre en continu jusqu'à ce qu'un accord ou une impasse soit atteint. */
public class NegotiationBehaviour extends CyclicBehaviour {
    private final KnowledgeBase kb;
    private final boolean isInitiator;
    private static final int MAX_TURNS = 15; // Sécurité pour éviter les négociations infinies
    private static final String CONV_ID = "negociation";

    private AID partner; // Destinataire (peut être null au départ pour l'agent répondeur)
    private Offer currentOffer; // L'offre courante de l'agent
    private Offer lastReceivedOffer;
    private int turn = 0;
    private boolean finished  = false; // Condition d'arrêt du CyclicBehaviour
    private boolean initiated = false; // Permet à l'initiateur de lancer la négociation une seule fois

    public NegotiationBehaviour(Agent agent, KnowledgeBase kb, AID partner, boolean isInitiator) {
        super(agent);
        this.kb = kb;
        this.partner = partner;
        this.isInitiator = isInitiator;
        this.currentOffer = kb.getInitialOffer(); // Initialisation avec la position de base de la KB
    }

    @Override
    public void action() {
        // Si la négociation est terminée, on bloque le comportement
        if (finished) {
            block();
            return;
        }

        // Si l'initiateur est Direction, alors envoie la première offre
        if (isInitiator && !initiated) {
            initiated = true;
            System.out.printf("%nDÉBUT DE LA NÉGOCIATION%n");
            sendOffer(currentOffer);
            return; // On sort pour laisser le temps au message de partir, on lira la réponse au prochain cycle
        }

        // Écoute des messages filtrés par l'ID de conversation
        MessageTemplate mt = MessageTemplate.MatchConversationId(CONV_ID);
        ACLMessage msg = myAgent.receive(mt);
        if (msg == null) { block(); return; } // Met l'agent en veille jusqu'au prochain message

        // Découverte dynamique du partenaire 
        if (partner == null) {
            partner = msg.getSender();
            System.out.printf("[%s] Partenaire découvert : %s%n", myAgent.getLocalName(), partner.getLocalName());
        }

        // Machine à états basée sur les speech acts
        switch (msg.getPerformative()) {
            case ACLMessage.PROPOSE:
                handlePropose(msg);
                break;
            case ACLMessage.ACCEPT_PROPOSAL:
                System.out.printf("[%s]Accord accepté par l'autre agent.%n", myAgent.getLocalName());
                finished = true;
                break;
            case ACLMessage.FAILURE:
                System.out.printf("[%s]Deadlock déclaré par l'autre agent.%n", myAgent.getLocalName());
                finished = true;
                break;
        }
    }

    // Traite la réception d'une offre (PROPOSE). Vérifie si l'acceptabilité, sinon calcule et envoie une contre-offre.
    private void handlePropose(ACLMessage msg) {
        try {
            // Désérialisation de l'objet Offer transmis dans le message ACL
            lastReceivedOffer = (Offer) msg.getContentObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.printf("[%s]Tour %-2d | Reçu    : %s%n", myAgent.getLocalName(), turn, lastReceivedOffer);
        
        if (kb.isAcceptable(lastReceivedOffer)) { // L'offre satisfait nos conditions minimales alors accord
            sendAccept();
        }
        else if (turn >= MAX_TURNS) { // On a dépassé la limite de tours sans trouver d'accord alors échec
            sendDeadlock();
        }
        else { // L'offre est inacceptable alors génère une contre-proposition
            // Calcul de la concession puis clamp par la KB pour ne jamais franchir les lignes rouges
            currentOffer = kb.clamp(ConcessionStrategy.concede(currentOffer, lastReceivedOffer));
            turn++;
            System.out.printf("[%s]Tour %-2d | Envoyé  : %s%n", myAgent.getLocalName(), turn, currentOffer);
            sendOffer(currentOffer);
        }
    }

    private void sendOffer(Offer offer) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.addReceiver(partner);
            msg.setConversationId(CONV_ID);
            msg.setContentObject(offer);
            myAgent.send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendAccept() {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(partner);
        msg.setConversationId(CONV_ID);
        msg.setContent("ACCORD");
        myAgent.send(msg);
        System.out.printf("%n[%s]ACCORD FINAL : %s%n", myAgent.getLocalName(), lastReceivedOffer);
        finished = true;
    }

    private void sendDeadlock() {
        ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);
        msg.addReceiver(partner);
        msg.setConversationId(CONV_ID);
        msg.setContent("DEADLOCK");
        myAgent.send(msg);
        System.out.printf("[%s]DEADLOCK après %d tours.%n", myAgent.getLocalName(), turn);
        finished = true;
    }
}
