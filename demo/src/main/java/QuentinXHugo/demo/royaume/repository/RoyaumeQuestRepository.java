package QuentinXHugo.demo.royaume.repository;

import QuentinXHugo.demo.royaume.dto.RoyaumeQuestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Repository
public class RoyaumeQuestRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoyaumeQuestRepository.class);

    private final RestClient royaumeRestClient;

    public RoyaumeQuestRepository(RestClient royaumeRestClient) {
        this.royaumeRestClient = royaumeRestClient;
    }

    public RoyaumeQuestResponse fetchQuest(String group) {
        try {
            return royaumeRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quests")
                            .queryParam("group", group)
                            .build())
                    .retrieve()
                    .body(RoyaumeQuestResponse.class);
        } catch (RestClientResponseException ex) {
            LOGGER.error("Failed to fetch quest for group {}: {} {}", group, ex.getStatusCode().value(), ex.getMessage());
            throw ex;
        }
    }

    public RoyaumeQuestResponse resolveQuest(String questId) {
        try {
            return royaumeRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quests/{id}/resolve")
                            .build(questId))
                    .retrieve()
                    .body(RoyaumeQuestResponse.class);
        } catch (RestClientResponseException ex) {
            LOGGER.error("Failed to resolve quest {}: {} {}", questId, ex.getStatusCode().value(), ex.getMessage());
            throw ex;
        }
    }

}
