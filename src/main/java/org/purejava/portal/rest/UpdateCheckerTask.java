package org.purejava.portal.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class UpdateCheckerTask extends FutureTask<String> {

    private static final String FLATHUB_API = "https://flathub.org/api/v2/appstream/";
    private static final ObjectMapper JSON = new ObjectMapper();

    public UpdateCheckerTask(String app) {
        super(new UpdateCheckerCallable(app != null ? app : ""));
    }

    private static class UpdateCheckerCallable implements Callable<String> {
        private final HttpClient httpClient;
        private final HttpRequest checkForUpdatesRequest;

        public UpdateCheckerCallable(String appId) {
            this.httpClient = HttpClient.newHttpClient();
            URI uri = URI.create(FLATHUB_API + appId);
            this.checkForUpdatesRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
        }

        @Override
        public String call() throws Exception {
            HttpResponse<InputStream> response = httpClient.send(checkForUpdatesRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                return getLatestRelease(response);
            } else {
                throw new IOException("Failed to check for updates: HTTP " + response.statusCode());
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
                    throw new IOException("No valid latest release information found");
                }
            }
        }
    }
}

