package com.climapulse.jceco.integration.inpe.client;

import com.climapulse.jceco.integration.inpe.config.InpeProperties;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotCsvFile;
import com.climapulse.jceco.shared.exception.InpeCsvClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class InpeHotspotCsvClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InpeHotspotCsvClient.class);

    private final InpeHotspotCsvFilenameGenerator filenameGenerator;
    private final RestClient restClient;

    public InpeHotspotCsvClient(
            InpeHotspotCsvFilenameGenerator filenameGenerator,
            InpeProperties inpeProperties,
            RestClient.Builder restClientBuilder
    ) {
        this.filenameGenerator = filenameGenerator;

        this.restClient = restClientBuilder
                .baseUrl(inpeProperties.csvBaseUrl().toString())
                .build();
    }

    public Optional<InpeHotspotCsvFile> fetchRecentCsv() {
        String filename = filenameGenerator.buildRecentFilename();

        try {
            return fetchCsvIfExists(filename);
        } catch (InpeCsvClientException exception) {
            LOGGER.warn(
                    "Could not fetch INPE CSV file: {}",
                    filename,
                    exception
            );

            return Optional.empty();
        }
    }

    public List<InpeHotspotCsvFile> fetchRecentCsvs() {
        List<InpeHotspotCsvFile> files = new ArrayList<>();

        for (String filename : filenameGenerator.buildRecentFilenames()) {
            try {
                fetchCsvIfExists(filename).ifPresent(files::add);
            } catch (InpeCsvClientException exception) {
                LOGGER.warn(
                        "Could not fetch INPE CSV file: {}",
                        filename,
                        exception
                );
            }
        }

        return files;
    }

    private Optional<InpeHotspotCsvFile> fetchCsvIfExists(
            String filename
    ) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(filename)
                            .build())
                    .exchange((_, response) -> {
                        int status = response.getStatusCode().value();

                        if (status == 404) {
                            return Optional.empty();
                        }

                        if (status != 200) {
                            throw new InpeCsvClientException(
                                    "Could not fetch INPE CSV. Status: "
                                            + status
                            );
                        }

                        String content = response.bodyTo(String.class);

                        if (content == null) {
                            throw new InpeCsvClientException(
                                    "INPE returned an empty response body"
                            );
                        }

                        return Optional.of(
                                new InpeHotspotCsvFile(
                                        filename,
                                        content
                                )
                        );
                    });
        } catch (InpeCsvClientException exception) {
            throw new InpeCsvClientException("Error communicating with INPE", exception);
        } catch (RestClientException exception) {
            throw new InpeCsvClientException(
                    "Could not communicate with INPE",
                    exception
            );
        }
    }
}