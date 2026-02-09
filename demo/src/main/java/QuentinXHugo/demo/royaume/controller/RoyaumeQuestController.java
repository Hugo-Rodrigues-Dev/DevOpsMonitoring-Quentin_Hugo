package QuentinXHugo.demo.royaume.controller;

import QuentinXHugo.demo.royaume.dto.RoyaumeQuestResponse;
import QuentinXHugo.demo.royaume.entity.QuestEntity;
import QuentinXHugo.demo.royaume.service.RoyaumeQuestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/royaume/quests")
public class RoyaumeQuestController {

    private final RoyaumeQuestService service;

    public RoyaumeQuestController(RoyaumeQuestService service) {
        this.service = service;
    }

    @GetMapping
    public List<QuestEntity> listQuests() {
        return service.listQuests();
    }

    @PostMapping("/fetch")
    public QuestEntity fetchQuest() {
        return service.fetchQuestFromProfessorAsync().join();
    }

    @PostMapping("/{id}/launch")
    public Map<String, Object> launchQuest(@PathVariable("id") String questId) {
        service.launchQuestResolution(questId);
        return Map.of(
                "questId", questId,
                "status", "LAUNCHED"
        );
    }

    @GetMapping("/{id}/launch")
    public Map<String, Object> launchQuestGet(@PathVariable("id") String questId) {
        return launchQuest(questId);
    }
}
