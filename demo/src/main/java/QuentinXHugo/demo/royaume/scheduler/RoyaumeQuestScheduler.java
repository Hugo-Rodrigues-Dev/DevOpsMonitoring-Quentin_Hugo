package QuentinXHugo.demo.royaume.scheduler;

import QuentinXHugo.demo.royaume.config.RoyaumeApiProperties;
import QuentinXHugo.demo.royaume.mode.RoyaumeModeService;
import QuentinXHugo.demo.royaume.service.RoyaumeQuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RoyaumeQuestScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoyaumeQuestScheduler.class);

    private final RoyaumeQuestService questService;
    private final RoyaumeApiProperties properties;
    private final RoyaumeModeService modeService;

    public RoyaumeQuestScheduler(RoyaumeQuestService questService,
                                 RoyaumeApiProperties properties,
                                 RoyaumeModeService modeService) {
        this.questService = questService;
        this.properties = properties;
        this.modeService = modeService;
    }

    @Scheduled(fixedDelayString = "${royaume.api.poll-interval:PT45S}")
    public void pollQuest() {
        questService.fetchQuestFromProfessorAsync()
                .exceptionally(ex -> {
                    LOGGER.warn("Failed to poll quest: {}", ex.getMessage());
                    return null;
                });
    }
}
