package QuentinXHugo.demo.scheduling;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import QuentinXHugo.demo.client.ProfessorClient;
import QuentinXHugo.demo.config.ProfessorProperties;
import QuentinXHugo.demo.dto.QuestPayload;
import QuentinXHugo.demo.model.Quest;
import QuentinXHugo.demo.service.QuestService;

@Component
public class QuestScheduler {

	private static final Logger log = LoggerFactory.getLogger(QuestScheduler.class);

	private final ProfessorClient professorClient;
	private final QuestService questService;
	private final ProfessorProperties properties;

	public QuestScheduler(ProfessorClient professorClient, QuestService questService, ProfessorProperties properties) {
		this.professorClient = professorClient;
		this.questService = questService;
		this.properties = properties;
	}

	@Scheduled(fixedDelayString = "${royaume.professor.fetch-delay:60000}", initialDelayString = "2000")
	public void fetchAndScheduleResolution() {
		Optional<QuestPayload> payloadOpt = professorClient.fetchQuest();
		if (payloadOpt.isEmpty()) {
			return;
		}

		QuestPayload payload = payloadOpt.get();
		questService.saveIfNeeded(payload).ifPresent(quest -> {
			if (!properties.isEnabled()) {
				log.info("Quest {} saved (auto-resolve disabled)", quest.getId());
				return;
			}
			Duration wait = questService.computeWaitDuration(payload);
			log.info("Scheduling quest {} resolution in {}", quest.getId(), wait);
			questService.processQuestAsync(quest, wait);
		});
	}
}
