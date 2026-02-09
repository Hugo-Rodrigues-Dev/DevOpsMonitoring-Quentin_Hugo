package QuentinXHugo.demo.royaume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Duration;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoyaumeQuest(
        String id,
        String kind,
        String titre,
        String description,
        String lieu,
        String ennemi,
        String priorite,
        String recompense,
        Duration dureeEstimee,
        Instant delaiLimite,
        Double latitude,
        Double longitude
) {
}
