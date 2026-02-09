package QuentinXHugo.demo.royaume.service;

import QuentinXHugo.demo.royaume.config.RoyaumeApiProperties;
import QuentinXHugo.demo.royaume.dto.RoyaumeQuest;
import QuentinXHugo.demo.royaume.dto.RoyaumeQuestResponse;
import QuentinXHugo.demo.royaume.entity.QuestEntity;
import QuentinXHugo.demo.royaume.model.QuestStatus;
import QuentinXHugo.demo.royaume.mode.RoyaumeModeService;
import QuentinXHugo.demo.royaume.repository.QuestJpaRepository;
import QuentinXHugo.demo.royaume.repository.RoyaumeQuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
public class RoyaumeQuestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoyaumeQuestService.class);

    private final RoyaumeQuestRepository royaumeQuestRepository;
    private final QuestJpaRepository questJpaRepository;
    private final RoyaumeApiProperties properties;
    private final Clock clock;
    private final RoyaumeModeService modeService;
    private final Executor questExecutor;

    public RoyaumeQuestService(RoyaumeQuestRepository royaumeQuestRepository,
                               QuestJpaRepository questJpaRepository,
                               RoyaumeApiProperties properties,
                               Clock clock,
                               RoyaumeModeService modeService,
                               @Qualifier("royaumeQuestExecutor") Executor questExecutor) {
        this.royaumeQuestRepository = royaumeQuestRepository;
        this.questJpaRepository = questJpaRepository;
        this.properties = properties;
        this.clock = clock;
        this.questExecutor = questExecutor;
        this.modeService = modeService;
    }

    @Async("royaumeQuestExecutor")
    public CompletableFuture<QuestEntity> fetchQuestFromProfessorAsync() {
        var resolvedGroup = properties.getDefaultGroup();
        var response = royaumeQuestRepository.fetchQuest(resolvedGroup);
        if (response == null || response.quest() == null) {
            return CompletableFuture.completedFuture(null);
        }
        var payload = response.quest();
        var questId = resolveQuestId(payload);
        var entity = questJpaRepository.findById(questId).orElseGet(QuestEntity::new);
        var isNew = entity.getId() == null;
        entity.setId(questId);
        applyQuestPayload(entity, payload, true);
        if (isNew) {
            entity.setStatus(QuestStatus.PENDING);
        }
        entity = questJpaRepository.save(entity);
        LOGGER.info("Fetched quest {} (status={}, new={})", entity.getId(), entity.getStatus(), isNew);
        if (modeService.isAuto() && entity.getStatus() == QuestStatus.PENDING) {
            startResolution(entity);
        }
        return CompletableFuture.completedFuture(entity);
    }

    public List<QuestEntity> listQuests() {
        return questJpaRepository.findByStatusInOrderByFetchedAtDesc(visibleStatuses());
    }

    public void launchQuestResolution(String questId) {
        var quest = questJpaRepository.findById(questId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quest not found: " + questId));
        if (QuestStatus.RESOLVED.equals(quest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quest already resolved");
        }
        if (QuestStatus.RUNNING.equals(quest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quest already running");
        }
        startResolution(quest);
    }

    private void startResolution(QuestEntity quest) {
        if (QuestStatus.RESOLVED.equals(quest.getStatus())) {
            LOGGER.debug("Quest {} already resolved, skipping resolution trigger", quest.getId());
            return;
        }
        if (QuestStatus.RUNNING.equals(quest.getStatus())) {
            LOGGER.debug("Quest {} already running, skip duplicate trigger", quest.getId());
            return;
        }
        quest.setStatus(QuestStatus.RUNNING);
        questJpaRepository.save(quest);
        var delay = computeDelay(quest);
        var millis = Math.max(delay.toMillis(), 0);
        LOGGER.info("Scheduling quest {} resolution in {} (mode {})", quest.getId(), delay, modeService.getMode());
        CompletableFuture.runAsync(
                () -> resolveQuestAfterDelay(quest.getId()),
                CompletableFuture.delayedExecutor(millis, TimeUnit.MILLISECONDS, questExecutor)
        );
    }

    private Duration computeDelay(QuestEntity quest) {
        var delay = quest.getDureeEstimee();
        if (delay == null || delay.isNegative() || delay.isZero()) {
            delay = properties.getDefaultResolveDelay();
        }
        var now = Instant.now(clock);
        var waitUntilDeadline = Duration.ZERO;
        if (quest.getDelaiLimite() != null) {
            waitUntilDeadline = Duration.between(now, quest.getDelaiLimite());
            if (waitUntilDeadline.isNegative()) {
                waitUntilDeadline = Duration.ZERO;
            }
        }
        return waitUntilDeadline.compareTo(delay) > 0 ? waitUntilDeadline : delay;
    }

    private void resolveQuestAfterDelay(String questId) {
        var quest = questJpaRepository.findById(questId).orElse(null);
        if (quest == null) {
            return;
        }
        try {
            var response = royaumeQuestRepository.resolveQuest(questId);
            if (response == null) {
                revertToPending(quest, "resolution returned empty response");
                return;
            }
            if (!response.ok()) {
                revertToPending(quest, "resolution refused: " + formatResponseError(response));
                return;
            }
            if (response.quest() != null) {
                applyQuestPayload(quest, response.quest(), false);
            }
            quest.setStatus(QuestStatus.RESOLVED);
            questJpaRepository.save(quest);
            LOGGER.info("Quest {} resolved and confirmed by professor (payloadSynced={})", questId, response.quest() != null);
        } catch (Exception ex) {
            revertToPending(quest, "resolution failed: " + ex.getMessage());
        }
    }

    private void revertToPending(QuestEntity quest, String reason) {
        quest.setStatus(QuestStatus.PENDING);
        questJpaRepository.save(quest);
        LOGGER.warn("Quest {} {} (mode={})", quest.getId(), reason, modeService.getMode());
    }

    private String resolveQuestId(RoyaumeQuest quest) {
        if (quest != null && StringUtils.hasText(quest.id())) {
            return quest.id();
        }
        return UUID.randomUUID().toString();
    }

    private void applyQuestPayload(QuestEntity entity, RoyaumeQuest quest, boolean refreshFetchTime) {
        if (entity == null || quest == null) {
            return;
        }
        entity.setKind(quest.kind());
        entity.setTitre(quest.titre());
        entity.setDescription(quest.description());
        entity.setLieu(quest.lieu());
        entity.setEnnemi(quest.ennemi());
        entity.setPriorite(quest.priorite());
        entity.setRecompense(quest.recompense());
        entity.setDureeEstimee(quest.dureeEstimee());
        entity.setDelaiLimite(quest.delaiLimite());
        entity.setLatitude(quest.latitude());
        entity.setLongitude(quest.longitude());
        if (refreshFetchTime || entity.getFetchedAt() == null) {
            entity.setFetchedAt(Instant.now(clock));
        }
    }

    private List<QuestStatus> visibleStatuses() {
        if (modeService.isAuto()) {
            return List.of(QuestStatus.PENDING, QuestStatus.RUNNING);
        }
        return List.of(QuestStatus.PENDING);
    }

    private String formatResponseError(RoyaumeQuestResponse response) {
        if (response == null) {
            return "unknown error";
        }
        var code = StringUtils.hasText(response.codeRetour()) ? response.codeRetour() : "NO_CODE";
        var message = StringUtils.hasText(response.errorMessage()) ? response.errorMessage() : "no details";
        return code + " - " + message;
    }
}
