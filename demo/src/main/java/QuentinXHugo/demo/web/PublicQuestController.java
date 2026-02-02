package QuentinXHugo.demo.web;

import java.time.Duration;
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

/**
 * Expose les endpoints attendus par le starter IHM (sans préfixe /api).
 */
@RestController
public class PublicQuestController {

	private final QuestService questService;

	public PublicQuestController(QuestService questService) {
		this.questService = questService;
	}

	@GetMapping("/quests")
	public List<Quest> list() {
		return questService.listAll();
	}

	@PostMapping("/quests/{id}/resolve")
	public ResponseEntity<Void> resolve(@PathVariable String id,
		@RequestParam(name = "delay", required = false) Duration delay) {
		return questService.findById(id)
			.map(quest -> {
				QuestPayload payload = new QuestPayload();
				payload.setDureeEstimee(quest.getDureeEstimee());
				questService.processQuestAsync(quest, delay != null ? delay : questService.computeWaitDuration(payload));
				return ResponseEntity.accepted().<Void>build();
			})
			.orElseGet(() -> ResponseEntity.notFound().<Void>build());
	}
}
