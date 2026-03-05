package QuentinXHugo.demo.model;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import QuentinXHugo.demo.persistence.DurationToLongConverter;

@Entity
@Table(name = "quests")
public class Quest {

	@Id
	private String id;

	@Column(length = 100)
	private String kind;
	@Column(length = 512)
	private String titre;
	@Column(columnDefinition = "TEXT")
	private String description;
	@Column(length = 512)
	private String lieu;
	@Column(length = 512)
	private String ennemi;
	@Column(length = 100)
	private String priorite;
	@Column(length = 100)
	private String recompense;
	@Convert(converter = DurationToLongConverter.class)
	@Column(name = "duree_estimee_ms")
	private Duration dureeEstimee;
	private Instant delaiLimite;
	private Double latitude;
	private Double longitude;

	@Enumerated(EnumType.STRING)
	private QuestStatus status = QuestStatus.RECEIVED;

	private Instant receivedAt;
	private Instant resolvedAt;

	@Column(length = 1024)
	private String lastError;

	@Version
	private long version;

	@PrePersist
	public void setReceivedAt() {
		if (receivedAt == null) {
			receivedAt = Instant.now();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLieu() {
		return lieu;
	}

	public void setLieu(String lieu) {
		this.lieu = lieu;
	}

	public String getEnnemi() {
		return ennemi;
	}

	public void setEnnemi(String ennemi) {
		this.ennemi = ennemi;
	}

	public String getPriorite() {
		return priorite;
	}

	public void setPriorite(String priorite) {
		this.priorite = priorite;
	}

	public String getRecompense() {
		return recompense;
	}

	public void setRecompense(String recompense) {
		this.recompense = recompense;
	}

	public Duration getDureeEstimee() {
		return dureeEstimee;
	}

	public void setDureeEstimee(Duration dureeEstimee) {
		this.dureeEstimee = dureeEstimee;
	}

	public Instant getDelaiLimite() {
		return delaiLimite;
	}

	public void setDelaiLimite(Instant delaiLimite) {
		this.delaiLimite = delaiLimite;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public QuestStatus getStatus() {
		return status;
	}

	public void setStatus(QuestStatus status) {
		this.status = status;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(Instant receivedAt) {
		this.receivedAt = receivedAt;
	}

	public Instant getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(Instant resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public long getVersion() {
		return version;
	}
}
