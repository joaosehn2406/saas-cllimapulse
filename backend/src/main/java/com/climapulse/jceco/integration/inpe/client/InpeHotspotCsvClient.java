package com.climapulse.jceco.integration.inpe.client;

import com.climapulse.jceco.shared.exception.InpeCsvClientException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class InpeHotspotCsvClient {

    private static final URI INPE_BASE_URI = URI.create(
            "https://dataserver-coids.inpe.br/queimadas/queimadas/focos/csv/10min/"
    );

    private final InpeHotspotCsvFilenameGenerator filenameGenerator;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public InpeHotspotCsvClient(InpeHotspotCsvFilenameGenerator filenameGenerator) {
        this.filenameGenerator = filenameGenerator;
    }

    public List<InpeHotspotCsvFile> fetchRecentCsvs() {
        var files = new ArrayList<InpeHotspotCsvFile>();

        for (String filename : filenameGenerator.buildRecentFilenames()) {
            var file = fetchCsvIfExists(filename);

            file.ifPresent(files::add);
        }

        return files;
    }

    private Optional<InpeHotspotCsvFile> fetchCsvIfExists(String filename) {
        var request = HttpRequest.newBuilder()
                .uri(INPE_BASE_URI.resolve(filename))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        var response = send(request);

        if (response.statusCode() == 404) {
            return Optional.empty();
        }

        if (response.statusCode() != 200) {
            throw new InpeCsvClientException(
                    "Could not fetch INPE CSV. Status: " + response.statusCode()
            );
        }

        return Optional.of(new InpeHotspotCsvFile(filename, response.body()));
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            throw new InpeCsvClientException("Could not communicate with INPE", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InpeCsvClientException("Interrupted while communicating with INPE", exception);
        }
    }
}