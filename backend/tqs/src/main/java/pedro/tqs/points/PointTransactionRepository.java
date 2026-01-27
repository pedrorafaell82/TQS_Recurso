package pedro.tqs.points;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    boolean existsByParticipation_Id(Long participationId);

    @Query("select coalesce(sum(t.amount), 0) from PointTransaction t where lower(t.user.email) = lower(:email)")
    int getBalanceForEmail(@Param("email") String email);
}
