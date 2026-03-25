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
    }
}
