package QuentinXHugo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import QuentinXHugo.demo.model.Quest;
import QuentinXHugo.demo.model.QuestStatus;

@Repository
public interface QuestRepository extends JpaRepository<Quest, String> {

	java.util.List<Quest> findByStatusNot(QuestStatus status, org.springframework.data.domain.Sort sort);

	java.util.List<Quest> findByStatus(QuestStatus status, org.springframework.data.domain.Sort sort);

	@Modifying
	@Transactional
	@Query("update Quest q set q.status = :status, q.resolvedAt = null, q.lastError = null "
		+ "where q.id = :id and q.status not in (:processing, :resolved)")
	int markProcessingIfIdle(@Param("id") String id,
		@Param("status") QuestStatus status,
		@Param("processing") QuestStatus processing,
		@Param("resolved") QuestStatus resolved);

	@Modifying
	@Transactional
	@Query("update Quest q set q.status = :status, q.resolvedAt = :resolvedAt, q.lastError = null where q.id = :id")
	int markResolved(@Param("id") String id,
		@Param("status") QuestStatus status,
		@Param("resolvedAt") java.time.Instant resolvedAt);

	@Modifying
	@Transactional
	@Query("update Quest q set q.status = :status, q.lastError = :error where q.id = :id")
	int markFailed(@Param("id") String id,
		@Param("status") QuestStatus status,
		@Param("error") String error);
}
