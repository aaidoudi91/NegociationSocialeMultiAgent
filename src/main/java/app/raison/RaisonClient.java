package app.raison;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Client HTTP vers l'API rAIson. Utilise HttpURLConnection
 * En cas d'erreur réseau ou de réponse vide, retourne "reject_offer" comme fallback sûr. */
public class RaisonClient {
    private static final String BASE_URL = "https://api.ai-raison.com/executions";
    private static final String VERSION = "latest";

    private final Gson gson = new Gson();
    private final String apiKey;
    private final String appId;
    private final List<RaisonOption> projectOptions;

    public RaisonClient(String apiKey, String appId, List<RaisonOption> projectOptions) {
        this.apiKey = apiKey;
        this.appId = appId;
        this.projectOptions = projectOptions;
    }

    public String query(List<RaisonElement> activeElements) {
        try {
            String body = gson.toJson(new RaisonRequest(activeElements, projectOptions));
            URL url = new URL(BASE_URL + "/" + appId + "/" + VERSION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
            String response;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                response = sb.toString();
            }

            if (status != 200) {
                System.err.printf("[RAISON] Erreur HTTP %d%n", status);
                return "reject_offer";
            }

            List<RaisonResult> results = gson.fromJson(
                    response,
                    new TypeToken<List<RaisonResult>>() {}.getType()
            );

            if (results == null || results.isEmpty()) {
                System.err.println("[RAISON] Aucune solution retournée.");
                return "reject_offer";
            }

            return results.stream()
                    .filter(RaisonResult::isSolution)
                    .map(r -> r.getOption().getLabel())
                    .findFirst()
                    .orElse("reject_offer");

        } catch (Exception e) {
            System.err.printf("[RAISON] Exception %s : %s%n",
                    e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();  // ← stack trace complète pour déboguer
            return "reject_offer";
        }
    }

    public void printMetadata() {
        try {
            URL url = new URL(BASE_URL + "/" + appId + "/" + VERSION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-api-key", apiKey);

            int status = conn.getResponseCode();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                System.out.printf("[RAISON] Metadata (status %d) :%n%s%n", status, sb);
            }
        } catch (Exception e) {
            System.err.printf("[RAISON] Erreur metadata : %s%n", e.getMessage());
        }
    }
}
