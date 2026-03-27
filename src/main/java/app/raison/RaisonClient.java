package app.raison;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/** Client HTTP pour l'API rAIson : chaque agent dispose de sa propre instance pointant vers son projet dédié.
 * En cas d'erreur HTTP ou d'exception réseau, retourne "reject_offer" par défaut afin de ne jamais bloquer JADE. */
public class RaisonClient {
    private static final String BASE_URL = "https://api.ai-raison.com/executions";
    private static final String VERSION  = "latest";

    private final HttpClient client;
    private final Gson gson;
    private final String apiKey;
    private final String appId;

    public RaisonClient(String apiKey, String appId) {
        this.apiKey = apiKey;
        this.appId = appId;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // Envoie les éléments actifs à rAIson et retourne le label de l'option gagnante.
    public String query(List<RaisonElement> activeElements) {
        try {
            RaisonRequest request = new RaisonRequest(activeElements);
            String body = gson.toJson(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + appId + "/" + VERSION))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey) // header d'authentification
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<RaisonResult> results = gson.fromJson(
                        response.body(),
                        new TypeToken<List<RaisonResult>>() {}.getType()
                );

                // On cherche la première option qui est une solution
                return results.stream()
                        .filter(RaisonResult::isSolution)
                        .map(r -> r.getOption().getLabel())
                        .findFirst()
                        .orElse("reject_offer"); // fallback sécurisé

            } else {
                System.err.println("[RAISON] Erreur HTTP " + response.statusCode()
                        + " : " + response.body());
                return "reject_offer";
            }

        } catch (Exception e) {
            System.err.println("[RAISON] Exception lors de l'appel API : " + e.getMessage());
            return "reject_offer";
        }
    }

    // Appelle le GET /{appId}/{version} pour afficher tous les éléments et options
    public void printMetadata() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + appId + "/" + VERSION))
                    .header("x-api-key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[RAISON] Metadata (status " + response.statusCode() + ") :");
            System.out.println(response.body());

        } catch (Exception e) {
            System.err.println("[RAISON] Erreur metadata : " + e.getMessage());
        }
    }
}
