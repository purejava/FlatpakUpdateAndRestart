package org.purejava.portal.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class UpdateCheckerTask {

    private static final String FLATHUB_API = "https://flathub.org/api/v2/appstream/";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String appId;
    private Duration delay = Duration.ZERO;

    private Runnable onRunning;
    private Consumer<String> onSucceeded;
    private Consumer<Throwable> onFailed;

    private Future<?> future;

    public UpdateCheckerTask(String appId) {
        this.appId = appId != null ? appId : "";
    }

    public void start() {
        if (future != null && !future.isDone()) return;

        future = CompletableFuture.runAsync(() -> {
            if (onRunning != null) onRunning.run();

            if (!delay.isZero()) {
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            try (var executor = Executors.newSingleThreadExecutor()) {
                Future<String> internal = executor.submit(this::checkForUpdates);

                String result = internal.get();
                if (onSucceeded != null) onSucceeded.accept(result);
            } catch (Throwable t) {
                if (onFailed != null) onFailed.accept(t);
            }
        });
    }

    public void cancel() {
        if (future != null) {
            future.cancel(true);
        }
    }

    public void reset() {
        cancel();
        future = null;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    public void setOnRunning(Runnable onRunning) {
        this.onRunning = onRunning;
    }

    public void setOnSucceeded(Consumer<String> onSucceeded) {
        this.onSucceeded = onSucceeded;
    }

    public void setOnFailed(Consumer<Throwable> onFailed) {
        this.onFailed = onFailed;
    }

    private String checkForUpdates() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var uri = URI.create(FLATHUB_API + appId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                return getLatestRelease(response);
            } else {
                throw new IOException("Failed to check for updates: HTTP " + response.statusCode());
            }
        }
    }

    private String getLatestRelease(HttpResponse<InputStream> response) throws IOException {
        try (InputStream in = response.body()) {
            var root = JSON.readTree(in);
            var releases = root.get("releases");
            if (releases == null || !releases.isArray()) {
                throw new IOException("'releases' array not found in response");
            }

            JsonNode latest = null;
            for (JsonNode release : releases) {
                if (latest == null || release.get("timestamp").asLong() > latest.get("timestamp").asLong()) {
                    latest = release;
                }
            }

            if (latest != null && latest.has("version")) {
                return latest.get("version").asText();
            } else {
                throw new IOException("No valid latest release found");
            }
        }
    }

    public String getAppId() {
        return appId;
    }
}
