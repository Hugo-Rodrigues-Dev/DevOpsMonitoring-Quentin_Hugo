package QuentinXHugo.demo.royaume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoyaumeQuestResponse(
        String codeRetour,
        RoyaumeQuest quest,
        String errorMessage,
        boolean ok
) {
}
