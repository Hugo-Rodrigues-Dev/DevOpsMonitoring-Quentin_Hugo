package QuentinXHugo.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfessorQuestResponse {

	private String codeRetour;
	private QuestPayload quest;
	private String errorMessage;
	@JsonProperty("ok")
	private Boolean ok;

	public String getCodeRetour() {
		return codeRetour;
	}

	public void setCodeRetour(String codeRetour) {
		this.codeRetour = codeRetour;
	}

	public QuestPayload getQuest() {
		return quest;
	}

	public void setQuest(QuestPayload quest) {
		this.quest = quest;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Boolean getOk() {
		return ok;
	}

	public void setOk(Boolean ok) {
		this.ok = ok;
	}
}
