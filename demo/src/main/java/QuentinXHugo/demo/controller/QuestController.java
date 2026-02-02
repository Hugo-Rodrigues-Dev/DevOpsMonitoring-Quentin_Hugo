package QuentinXHugo.demo.controller;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuentinXHugo.demo.dto.QuestPayload;
import QuentinXHugo.demo.model.Quest;
import QuentinXHugo.demo.service.QuestService;

@RestController
public class QuestController {

	private final QuestService questService;

	public QuestController(QuestService questService) {
		this.questService = questService;
	}

	@GetMapping({ "/quests", "/api/quests", "/api/royaume/quests" })
	public List<Quest> list() {
		return questService.listAll();
	}

	@PostMapping({
		"/quests/{id}/resolve",
		"/api/quests/{id}/resolve",
		"/api/royaume/quests/{id}/resolve",
		"/api/royaume/quests/{id}/launch"
	})
	public ResponseEntity<Void> resolve(@PathVariable String id,
		@RequestParam(name = "delay", required = false) String delay,
		@RequestParam(name = "delayMs", required = false) Long delayMs) {
		Duration parsedDelay = parseDelay(delay, delayMs);
		return questService.findById(id)
			.map(quest -> {
				QuestPayload payload = new QuestPayload();
				payload.setDureeEstimee(quest.getDureeEstimee());
				questService.processQuestAsync(quest, parsedDelay != null ? parsedDelay : questService.computeWaitDuration(payload));
				return ResponseEntity.accepted().<Void>build();
			})
			.orElseGet(() -> ResponseEntity.notFound().<Void>build());
	}

	private Duration parseDelay(String delay, Long delayMs) {
		if (delayMs != null && delayMs >= 0) {
			return Duration.ofMillis(delayMs);
		}
		if (delay == null || delay.isBlank()) {
			return null;
		}
		try {
			return Duration.parse(delay);
		}
		catch (DateTimeParseException ex) {
			try {
				long seconds = Long.parseLong(delay);
				if (seconds < 0) {
					return null;
				}
				return Duration.ofSeconds(seconds);
			}
			catch (NumberFormatException ignored) {
				return null;
			}
		}
	}
}
