package QuentinXHugo.demo.scheduling;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import QuentinXHugo.demo.config.ProfessorProperties;
import QuentinXHugo.demo.model.Quest;
import QuentinXHugo.demo.model.QuestStatus;
import QuentinXHugo.demo.repository.QuestRepository;
import QuentinXHugo.demo.service.QuestService;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class QuestRecoveryRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(QuestRecoveryRunner.class);

	private final QuestRepository repository;
	private final QuestService questService;
	private final ProfessorProperties properties;

	public QuestRecoveryRunner(QuestRepository repository, QuestService questService, ProfessorProperties properties) {
		this.repository = repository;
		this.questService = questService;
		this.properties = properties;
	}

	@Override
	public void run(org.springframework.boot.ApplicationArguments args) {
		if (!properties.isEnabled()) {
			return;
		}
		List<Quest> processing = repository.findByStatus(QuestStatus.PROCESSING, Sort.by(Sort.Direction.DESC, "receivedAt"));
		if (processing.isEmpty()) {
			return;
		}
		log.info("Rescheduling {} processing quests after restart", processing.size());
		for (Quest quest : processing) {
			questService.rescheduleProcessing(quest);
		}
	}
}
