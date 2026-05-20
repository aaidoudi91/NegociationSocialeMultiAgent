package app.behaviours;

import app.model.Dimension;
import app.model.KnowledgeBase;
import app.model.Offer;
import app.raison.ElementBuilder;
import app.raison.RaisonClient;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** Comportement JADE gérant la boucle de négociation multi-tours. Implémenté en tant que CyclicBehaviour pour écouter
 *  et répondre en continu jusqu'à ce qu'un accord ou une impasse soit atteint.
 *  À chaque tour, rAIson est consulté pour décider de l'action à entreprendre. */
public class NegotiationBehaviour extends CyclicBehaviour {

    private final KnowledgeBase kb;
    private final boolean isInitiateur;
    private final RaisonClient raisonClient;

    private static final int TOURS_MAX = 7;
    private static final String CONV_ID = "negociation";

    private AID destinataire;
    private Offer offreCourante;
    private Offer derniereOffreRecue;
    private int tour = 0;
    private boolean termine = false; // Condition d'arrêt du CyclicBehaviour
    private boolean initie = false; // Permet à l'initiateur de lancer une seule fois

    public NegotiationBehaviour(Agent agent, KnowledgeBase kb, AID partenaire, boolean isInitiateur,
                                RaisonClient raisonClient) {
        super(agent);
        this.kb = kb;
        this.destinataire = partenaire;
        this.isInitiateur = isInitiateur;
        this.raisonClient = raisonClient;
        this.offreCourante = kb.getOffreInitiale();
    }

    @Override
    public void action() {
        if (termine) {
            block();
            return;
        }
        
        if (isInitiateur && !initie) { // L'initiateur envoie la première offre sans attendre de message
            initie = true;
            System.out.printf("%nDÉBUT DE LA NÉGOCIATION%n");
            envoyerOffre(offreCourante);
            return;
        }

        // Écoute des messages filtrés par l'ID de conversation
        MessageTemplate mt = MessageTemplate.MatchConversationId(CONV_ID);
        ACLMessage message = myAgent.receive(mt);
        if (message == null) {
            block();
            return;
        }
        
        if (destinataire == null) { // Découverte du partenaire depuis le premier message reçu (côté Syndicat)
            destinataire = message.getSender();
            System.out.printf("[%s] Partenaire découvert : %s%n", myAgent.getLocalName(), destinataire.getLocalName());
        }

        switch (message.getPerformative()) {
            case ACLMessage.PROPOSE:
                traiterProposition(message);
                break;
            case ACLMessage.ACCEPT_PROPOSAL:
                System.out.printf("[%s] Accord accepté par l'autre agent.%n", myAgent.getLocalName());
                termine = true;
                break;
            case ACLMessage.FAILURE:
                System.out.printf("[%s] Impasse déclarée par l'autre agent.%n", myAgent.getLocalName());
                termine = true;
                break;
        }
    }

    // Traite la réception d'une offre (PROPOSE) : construit les éléments actifs, consulte rAIson, puis agit selon
    // la décision retournée.
    private void traiterProposition(ACLMessage message) {
        try {
            derniereOffreRecue = (Offer) message.getContentObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.printf("[%s] Tour %-2d | Reçu : %s%n", myAgent.getLocalName(), tour, derniereOffreRecue);

        // Vérification directe via la KB : si toutes les conditions minimales sont atteintes, accord immédiat
        if (kb.estAcceptable(derniereOffreRecue)) {
            envoyerAccord();
            return;
        }
        
        if (tour >= TOURS_MAX) {  // Si dernier tour atteint alors impasse forcée
            signalerImpasse();
            return;
        }

        // Décision via Java (si 1 échec) ou rAIson (si n échecs)
        String decision = isInitiateur ? deciderDirection(derniereOffreRecue) : deciderSyndicat(derniereOffreRecue);
        System.out.printf("[%s] Décision : %s%n", myAgent.getLocalName(), decision);
        switch (decision) {
            case "accept_offer":
                envoyerAccord();
                return;
            case "counter_propose_requalification":
                offreCourante = kb.brider(ConcessionStrategy.concederSur(offreCourante, derniereOffreRecue,
                        Dimension.DUREE_REQUALIFICATION, 0.40, kb));
                break;
            case "counter_propose_compensation":
            case "counter_propose_budget":
                offreCourante = kb.brider(ConcessionStrategy.concederSur(offreCourante, derniereOffreRecue,
                        Dimension.COMPENSATION, 0.40, kb));
                break;
            case "counter_propose_timeline":
                offreCourante = kb.brider(ConcessionStrategy.concederSur(offreCourante, derniereOffreRecue,
                        Dimension.RYTHME_DEPLOIEMENT, 0.40, kb));
                break;
            case "counter_propose_jobs":
                offreCourante = kb.brider(ConcessionStrategy.concederSur(offreCourante, derniereOffreRecue,
                        Dimension.POSTES_SUPPRIMES, 0.40, kb));
                break;
            case "reject_offer":
                // Fallback RAISON (erreur réseau / crédits épuisés) → concession uniforme
                // L'impasse réelle est gérée par JADE
                offreCourante = kb.brider(ConcessionStrategy.conceder(offreCourante, derniereOffreRecue, kb));
                break;
            case "counter_propose_all":
            default:
                offreCourante = kb.brider(ConcessionStrategy.conceder(offreCourante, derniereOffreRecue, kb));
                break;
        }

        tour++;
        System.out.printf("[%s] Tour %-2d | Envoyé  : %s%n",myAgent.getLocalName(), tour, offreCourante);
        envoyerOffre(offreCourante);
    }

    // Détermine la stratégie de contre-offre sans systématiquement appeler rAIson. Si une seule dimension est en échec
    // alors décision Java directe, si plusieurs dimensions sont en échec alors délégation à rAIson pour identifier la
    // concession la plus pertinente parmi plusieurs conflits.
    private String deciderSyndicat(Offer r) {
        Offer min = kb.getOffreMinAcceptable();
        boolean postesOk = r.getPostesSupprimes() <= min.getPostesSupprimes();
        boolean requalifOk = r.getDureeRequalification() >= min.getDureeRequalification();
        boolean compensOk = r.getCompensationMois() >= min.getCompensationMois();
        boolean rythmeOk = r.getRythmeDeploiement() >= min.getRythmeDeploiement();
        boolean prioriteOk = r.isPrioriteRecrutement();

        int nbEchecs = (postesOk ? 0:1) + (requalifOk ? 0:1) + (compensOk ? 0:1) + (rythmeOk ? 0:1) +(prioriteOk ? 0:1);

        if (nbEchecs == 1) {
            if (!requalifOk) return "counter_propose_requalification";
            if (!compensOk) return "counter_propose_compensation";
            return "counter_propose_all"; // postes, rythme ou priorité seul
        }
        return raisonClient.query(ElementBuilder.forSyndicat(r, kb));
    }

    // Décision de la Direction : même logique que deciderSyndicat mais sur quatre dimensions numériques.
    private String deciderDirection(Offer r) {
        Offer min = kb.getOffreMinAcceptable();
        boolean postesOk = r.getPostesSupprimes() >= min.getPostesSupprimes();
        boolean compensOk = r.getCompensationMois() <= min.getCompensationMois();
        boolean rythmeOk = r.getRythmeDeploiement() <= min.getRythmeDeploiement();
        boolean requalifOk = r.getDureeRequalification() <= min.getDureeRequalification();

        int nbEchecs = (postesOk ? 0 : 1) + (compensOk  ? 0 : 1) + (rythmeOk   ? 0 : 1) + (requalifOk ? 0 : 1);

        if (nbEchecs == 1) {
            if (!rythmeOk) return "counter_propose_timeline";
            if (!compensOk) return "counter_propose_budget";
            if (!postesOk) return "counter_propose_jobs";
            return "counter_propose_all"; 
        }
        return raisonClient.query(ElementBuilder.forDirection(r, kb));
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
        synchronized (System.out) {
            System.out.println("\n════════════════════════════════════════");
            System.out.println("         ACCORD FINAL SIGNÉ             ");
            System.out.printf("  Postes supprimés   : %d%n",    derniereOffreRecue.getPostesSupprimes());
            System.out.printf("  Requalification    : %d mois%n", derniereOffreRecue.getDureeRequalification());
            System.out.printf("  Compensation       : %d mois%n", derniereOffreRecue.getCompensationMois());
            System.out.printf("  Rythme déploiement : %d mois%n", derniereOffreRecue.getRythmeDeploiement());
            System.out.printf("  Priorité recrut.   : %b%n",    derniereOffreRecue.isPrioriteRecrutement());
            System.out.printf("  Comité de suivi    : %b%n",    derniereOffreRecue.isComiteSuivi());
            System.out.println("════════════════════════════════════════");
        }
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
