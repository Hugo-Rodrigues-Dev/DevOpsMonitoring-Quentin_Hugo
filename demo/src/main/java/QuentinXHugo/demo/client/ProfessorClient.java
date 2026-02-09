package QuentinXHugo.demo.client;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import QuentinXHugo.demo.config.ProfessorProperties;
import QuentinXHugo.demo.dto.ProfessorQuestResponse;
import QuentinXHugo.demo.dto.QuestPayload;

@Component
public class ProfessorClient {

	private static final Logger log = LoggerFactory.getLogger(ProfessorClient.class);

	private final RestTemplate restTemplate;
	private final ProfessorProperties properties;

	public ProfessorClient(RestTemplate restTemplate, ProfessorProperties properties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	public Optional<QuestPayload> fetchQuest() {
		URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
			.path("/api/quests")
			.queryParam("group", properties.getGroup())
			.build(true)
			.toUri();
		try {
			ResponseEntity<ProfessorQuestResponse> response = restTemplate.getForEntity(uri, ProfessorQuestResponse.class);
			ProfessorQuestResponse body = response.getBody();
			if (response.getStatusCode().is2xxSuccessful() && body != null && Boolean.TRUE.equals(body.getOk())
				&& body.getQuest() != null) {
				log.info("Fetched quest from professor questId={} group={}", body.getQuest().getId(), properties.getGroup());
				return Optional.of(body.getQuest());
			}
			log.warn("Failed to fetch quest from professor status={} codeRetour={} error={} group={}", response.getStatusCode(), body != null ? body.getCodeRetour() : null,
				body != null ? body.getErrorMessage() : null);
			return Optional.empty();
		}
		catch (RestClientException ex) {
			log.warn("Fetch quest call failed group={} error={}", properties.getGroup(), ex.getMessage());
			return Optional.empty();
		}
	}

	public boolean resolveQuest(String questId) {
		URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
			.path("/api/quests/{id}/resolve")
			.buildAndExpand(questId)
			.toUri();
		try {
			ResponseEntity<ProfessorQuestResponse> response = restTemplate.postForEntity(uri, null, ProfessorQuestResponse.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				log.warn("Quest resolution HTTP failure questId={} status={}", questId, response.getStatusCode());
				return false;
			}
			ProfessorQuestResponse body = response.getBody();
			if (body == null) {
				log.warn("Quest resolution response missing body questId={}", questId);
				return false;
			}
			if (!Boolean.TRUE.equals(body.getOk())) {
				log.warn("Quest resolution rejected questId={} codeRetour={} error={}", questId, body.getCodeRetour(), body.getErrorMessage());
				return false;
			}
			if (body.getCodeRetour() != null && !"OK".equalsIgnoreCase(body.getCodeRetour())) {
				log.warn("Quest resolution returned non-OK code questId={} codeRetour={}", questId, body.getCodeRetour());
				return false;
			}
			log.info("Quest resolved successfully questId={}", questId);
			return true;
		}
		catch (RestClientException ex) {
			log.warn("Quest resolution call failed questId={} error={}", questId, ex.getMessage());
			return false;
		}
	}
}
