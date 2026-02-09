package QuentinXHugo.demo.royaume.repository;

import QuentinXHugo.demo.royaume.entity.QuestEntity;
import QuentinXHugo.demo.royaume.model.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestJpaRepository extends JpaRepository<QuestEntity, String> {
    List<QuestEntity> findByStatusInOrderByFetchedAtDesc(List<QuestStatus> statuses);
}
