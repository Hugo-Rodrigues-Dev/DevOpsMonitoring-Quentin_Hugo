package QuentinXHugo.demo.config;

import QuentinXHugo.demo.model.QuestStatus;
import QuentinXHugo.demo.repository.QuestRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class ObservabilityMetricsConfiguration {

	private final QuestRepository questRepository;
	private final MeterRegistry meterRegistry;

	public ObservabilityMetricsConfiguration(QuestRepository questRepository, MeterRegistry meterRegistry) {
		this.questRepository = questRepository;
		this.meterRegistry = meterRegistry;
	}

	@PostConstruct
	public void registerQuestStatusGauges() {
		for (QuestStatus status : QuestStatus.values()) {
			meterRegistry.gauge("quest_status_count", Tags.of("status", status.name()), questRepository,
				repo -> repo.countByStatus(status));
		}
	}
}
