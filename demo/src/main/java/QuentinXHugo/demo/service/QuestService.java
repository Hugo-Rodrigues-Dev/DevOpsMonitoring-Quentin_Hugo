package QuentinXHugo.demo.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import QuentinXHugo.demo.client.ProfessorClient;
import QuentinXHugo.demo.config.ProfessorProperties;
import QuentinXHugo.demo.dto.QuestPayload;
import QuentinXHugo.demo.model.Quest;
import QuentinXHugo.demo.model.QuestStatus;
import QuentinXHugo.demo.repository.QuestRepository;

@Service
public class QuestService {

	private static final Logger log = LoggerFactory.getLogger(QuestService.class);

	private final QuestRepository repository;
	private final ProfessorClient professorClient;
	private final ProfessorProperties properties;

	public QuestService(QuestRepository repository, ProfessorClient professorClient, ProfessorProperties properties) {
		this.repository = repository;
		this.professorClient = professorClient;
		this.properties = properties;
	}

	@Transactional
	public Optional<Quest> saveIfNeeded(QuestPayload payload) {
		if (payload == null || !StringUtils.hasText(payload.getId())) {
			return Optional.empty();
		}
		Quest quest = repository.findById(payload.getId()).orElse(new Quest());
		if (quest.getStatus() == QuestStatus.PROCESSING || quest.getStatus() == QuestStatus.RESOLVED) {
			return Optional.empty();
		}

		quest.setId(payload.getId());
		quest.setKind(payload.getKind());
		quest.setTitre(payload.getTitre());
		quest.setDescription(payload.getDescription());
		quest.setLieu(payload.getLieu());
		quest.setEnnemi(payload.getEnnemi());
		quest.setPriorite(payload.getPriorite());
		quest.setRecompense(payload.getRecompense());
		quest.setDureeEstimee(payload.getDureeEstimee());
		quest.setDelaiLimite(payload.getDelaiLimite());
		quest.setLatitude(payload.getLatitude());
		quest.setLongitude(payload.getLongitude());
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

	@Transactional
	public Optional<Quest> markProcessing(String questId) {
		return repository.findById(questId).map(quest -> {
			quest.setStatus(QuestStatus.PROCESSING);
			quest.setResolvedAt(null);
			quest.setLastError(null);
			return quest;
		});
	}

	@Transactional
	public void markResolved(String questId) {
		repository.findById(questId).ifPresent(quest -> {
			quest.setStatus(QuestStatus.RESOLVED);
			quest.setResolvedAt(Instant.now());
			quest.setLastError(null);
		});
	}

	@Transactional
	public void markFailed(String questId, String error) {
		repository.findById(questId).ifPresent(quest -> {
			quest.setStatus(QuestStatus.FAILED);
			quest.setLastError(error);
		});
	}

	public List<Quest> listAll() {
		return repository.findAll(Sort.by(Sort.Direction.DESC, "receivedAt"));
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
		markProcessing(quest.getId());
		doAsyncResolve(quest.getId(), waitDuration);
	}

	@Async
	protected void doAsyncResolve(String questId, Duration waitDuration) {
		Duration safeWait = waitDuration != null ? waitDuration : properties.getProcessingDelay();
		try {
			Thread.sleep(Math.max(safeWait.toMillis(), 0));
			boolean resolved = professorClient.resolveQuest(questId);
			if (resolved) {
				markResolved(questId);
			}
			else {
				markFailed(questId, "Professor service returned a non-success status");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			markFailed(questId, "Processing interrupted");
		}
		catch (Exception e) {
			log.warn("Error while processing quest {}: {}", questId, e.getMessage());
			markFailed(questId, e.getMessage());
		}
	}
}
