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
		if (!properties.isEnabled()) {
			return Optional.empty();
		}
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
				log.info("Quest fetched from professor: {}", body.getQuest().getId());
				return Optional.of(body.getQuest());
			}
			log.warn("Unable to fetch quest: status={} codeRetour={} error={}", response.getStatusCode(), body != null ? body.getCodeRetour() : null,
				body != null ? body.getErrorMessage() : null);
			return Optional.empty();
		}
		catch (RestClientException ex) {
			log.warn("Call to professor service failed: {}", ex.getMessage());
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
			boolean ok = response.getStatusCode().is2xxSuccessful();
			log.info("Quest {} resolution call returned status {}", questId, response.getStatusCode());
			return ok;
		}
		catch (RestClientException ex) {
			log.warn("Failed to resolve quest {}: {}", questId, ex.getMessage());
			return false;
		}
	}
}
