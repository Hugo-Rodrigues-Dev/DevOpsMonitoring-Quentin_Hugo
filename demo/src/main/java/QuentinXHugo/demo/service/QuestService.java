package QuentinXHugo.demo.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import QuentinXHugo.demo.client.ProfessorClient;
import QuentinXHugo.demo.config.ProfessorProperties;
import QuentinXHugo.demo.dto.QuestPayload;
import QuentinXHugo.demo.mapper.QuestMapper;
import QuentinXHugo.demo.model.Quest;
import QuentinXHugo.demo.model.QuestStatus;
import QuentinXHugo.demo.repository.QuestRepository;

@Service
public class QuestService {

	private static final Logger log = LoggerFactory.getLogger(QuestService.class);

	private final QuestRepository repository;
	private final ProfessorClient professorClient;
	private final ProfessorProperties properties;
	private final TaskScheduler taskScheduler;
	private final QuestMapper questMapper;

	public QuestService(QuestRepository repository, ProfessorClient professorClient, ProfessorProperties properties,
		TaskScheduler taskScheduler, QuestMapper questMapper) {
		this.repository = repository;
		this.professorClient = professorClient;
		this.properties = properties;
		this.taskScheduler = taskScheduler;
		this.questMapper = questMapper;
	}

	public Optional<Quest> saveIfNeeded(QuestPayload payload) {
		if (payload == null || !StringUtils.hasText(payload.getId())) {
			return Optional.empty();
		}
		Quest quest = repository.findById(payload.getId()).orElse(new Quest());
		if (quest.getStatus() == QuestStatus.PROCESSING || quest.getStatus() == QuestStatus.RESOLVED) {
			return Optional.empty();
		}

		questMapper.applyPayload(quest, payload);
		quest.setStatus(QuestStatus.RECEIVED);
		quest.setLastError(null);

		return Optional.of(repository.save(quest));
	}

	public Duration computeWaitDuration(QuestPayload payload) {
		Duration candidate = payload != null ? payload.getDureeEstimee() : null;
		if (candidate != null && !candidate.isNegative() && !candidate.isZero()) {
			return candidate;
		}
		return properties.getProcessingDelay();
	}

	public void markResolved(String questId) {
		repository.markResolved(questId, QuestStatus.RESOLVED, Instant.now());
	}

	public void markFailed(String questId, String error) {
		repository.markFailed(questId, QuestStatus.FAILED, error);
	}

	public List<Quest> listAll() {
		return repository.findByStatusNot(QuestStatus.RESOLVED, Sort.by(Sort.Direction.DESC, "receivedAt"));
	}

	public Optional<Quest> findById(String id) {
		return repository.findById(id);
	}

	public void processQuestAsync(Quest quest, Duration waitDuration) {
		if (quest == null) {
			return;
		}
		if (quest.getStatus() == QuestStatus.PROCESSING || quest.getStatus() == QuestStatus.RESOLVED) {
			return;
		}
		if (!tryMarkProcessing(quest.getId())) {
			log.info("Quest {} already processing or resolved, skipping scheduling", quest.getId());
			return;
		}
		Duration safeWait = waitDuration != null ? waitDuration : properties.getProcessingDelay();
		if (safeWait.isNegative()) {
			safeWait = Duration.ZERO;
		}
		Instant scheduledAt = Instant.now().plus(safeWait);
		taskScheduler.schedule(() -> resolveQuest(quest.getId()), scheduledAt);
		log.info("Quest {} scheduled for resolution at {}", quest.getId(), scheduledAt);
	}

	protected boolean tryMarkProcessing(String questId) {
		int updated = repository.markProcessingIfIdle(questId, QuestStatus.PROCESSING, QuestStatus.PROCESSING, QuestStatus.RESOLVED);
		return updated > 0;
	}

	protected void resolveQuest(String questId) {
		try {
			boolean resolved = professorClient.resolveQuest(questId);
			if (resolved) {
				markResolved(questId);
			}
			else {
				markFailed(questId, "Professor service returned a non-success status");
			}
		}
		catch (Exception e) {
			log.warn("Error while processing quest {}: {}", questId, e.getMessage());
			markFailed(questId, e.getMessage());
		}
	}
}
