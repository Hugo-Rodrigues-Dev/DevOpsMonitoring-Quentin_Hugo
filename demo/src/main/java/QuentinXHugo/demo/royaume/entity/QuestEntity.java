package QuentinXHugo.demo.royaume.entity;

import QuentinXHugo.demo.royaume.model.QuestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "quests")
public class QuestEntity {

    @Id
    private String id;

    private String kind;
    private String titre;

    @Column(length = 2000)
    private String description;

    private String lieu;
    private String ennemi;
    private String priorite;
    private String recompense;
    private Duration dureeEstimee;
    private Instant delaiLimite;
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Instant fetchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestStatus status = QuestStatus.PENDING;

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

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }
}
