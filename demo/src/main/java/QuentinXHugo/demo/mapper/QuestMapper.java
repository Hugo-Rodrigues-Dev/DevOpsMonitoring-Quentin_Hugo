package QuentinXHugo.demo.mapper;

import org.springframework.stereotype.Component;

import QuentinXHugo.demo.dto.QuestPayload;
import QuentinXHugo.demo.model.Quest;

@Component
public class QuestMapper {

	public void applyPayload(Quest quest, QuestPayload payload) {
		if (quest == null || payload == null) {
			return;
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
	}
}
