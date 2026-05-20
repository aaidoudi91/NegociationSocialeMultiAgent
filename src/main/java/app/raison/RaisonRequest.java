package app.raison;

import java.util.List;

/** Body du POST /executions/{appId}/latest envoyé à l'API rAIson. */
public class RaisonRequest {
    private List<RaisonElement> elements;
    private List<RaisonOption> options;

    public RaisonRequest(List<RaisonElement> elements, List<RaisonOption> options) {
        this.elements = elements;
        this.options  = options;
    }

    public List<RaisonElement> getElements() { 
        return elements; 
    }
    
    public List<RaisonOption> getOptions() { 
        return options;  
    }
}
