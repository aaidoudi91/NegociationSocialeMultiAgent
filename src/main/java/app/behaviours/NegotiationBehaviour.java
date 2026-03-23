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
    private final boolean isInitiateur;
    private static final int TOURS_MAX = 15; // Sécurité pour éviter les négociations infinies
    private static final String CONV_ID = "negociation";

    private AID destinataire; // Peut être null au départ pour l'agent répondeur
    private Offer offreCourante;
    private Offer derniereOffreRecue;
    private int tour = 0;
    private boolean termine = false; // Condition d'arrêt du CyclicBehaviour
    private boolean initie = false; // Permet à l'initiateur de lancer la négociation une seule fois

    public NegotiationBehaviour(Agent agent, KnowledgeBase kb, AID partenaire, boolean isInitiateur) {
        super(agent);
        this.kb = kb;
        this.destinataire = partenaire;
        this.isInitiateur = isInitiateur;
        this.offreCourante = kb.getOffreInitiale(); // Initialisation avec la position de base de la KB
    }

    @Override
    public void action() {
        if (termine) { // Si la négociation est terminée, on bloque le comportement
            block();
            return;
        }

        if (isInitiateur && !initie) { // Si l'initiateur est Direction, alors envoie la première offre
            initie = true;
            System.out.printf("%nDÉBUT DE LA NÉGOCIATION%n");
            envoyerOffre(offreCourante);
            return; // On sort pour laisser le temps au message de partir, on lira la réponse au prochain cycle
        }

        // Écoute des messages filtrés par l'ID de conversation
        MessageTemplate mt = MessageTemplate.MatchConversationId(CONV_ID);
        ACLMessage message = myAgent.receive(mt);
        if (message == null) {
            block(); // Met l'agent en veille jusqu'au prochain message
            return;
        }

        if (destinataire == null) { // Découverte du partenaire
            destinataire = message.getSender();
            System.out.printf("[%s] Partenaire découvert : %s%n", myAgent.getLocalName(), destinataire.getLocalName());
        }

        switch (message.getPerformative()) { // Choix des speech acts
            case ACLMessage.PROPOSE:
                traiterProposition(message);
                break;
            case ACLMessage.ACCEPT_PROPOSAL:
                System.out.printf("[%s] Accord accepté par l'autre agent.%n", myAgent.getLocalName());
                termine = true;
                break;
            case ACLMessage.FAILURE:
                System.out.printf("[%s] Impasse déclaré par l'autre agent.%n", myAgent.getLocalName());
                termine = true;
                break;
        }
    }

    // Traite la réception d'une offre (PROPOSE). Vérifie si l'acceptabilité, sinon calcule et envoie une contre-offre.
    private void traiterProposition(ACLMessage message) {
        try {
            derniereOffreRecue = (Offer) message.getContentObject(); // Désérialisation de l'objet Offer transmis
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.printf("[%s] Tour %d | Reçu : %s%n", myAgent.getLocalName(), tour, derniereOffreRecue);

        if (kb.estAcceptable(derniereOffreRecue)) { // L'offre satisfait nos conditions minimales alors accord
            envoyerAccord();
        } else if (tour >= TOURS_MAX) { // On a dépassé la limite de tours sans trouver d'accord alors échec
            signalerImpasse();
        } else { // L'offre est inacceptable alors génère une contre-proposition
            // Calcul de la concession puis brider par la KB pour ne jamais franchir les lignes rouges
            offreCourante = kb.brider(ConcessionStrategy.conceder(offreCourante, derniereOffreRecue));
            tour++;
            System.out.printf("[%s] Tour %d | Envoyé : %s%n", myAgent.getLocalName(), tour, offreCourante);
            envoyerOffre(offreCourante);
        }
    }

    private void envoyerOffre(Offer offer) {
        try {
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.addReceiver(destinataire);
            message.setConversationId(CONV_ID);
            message.setContentObject(offer);
            myAgent.send(message);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void envoyerAccord() {
        ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        message.addReceiver(destinataire);
        message.setConversationId(CONV_ID);
        message.setContent("ACCORD");
        myAgent.send(message);
        System.out.printf("%n[%s] ACCORD FINAL : %s%n", myAgent.getLocalName(), derniereOffreRecue);
        termine = true;
    }

    private void signalerImpasse() {
        ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
        message.addReceiver(destinataire);
        message.setConversationId(CONV_ID);
        message.setContent("IMPASSE");
        myAgent.send(message);
        System.out.printf("[%s] IMPASSE après %d tours.%n", myAgent.getLocalName(), tour);
        termine = true;
    }
}
