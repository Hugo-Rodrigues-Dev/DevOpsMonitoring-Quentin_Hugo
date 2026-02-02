package QuentinXHugo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import QuentinXHugo.demo.model.Quest;

@Repository
public interface QuestRepository extends JpaRepository<Quest, String> {
}
