package wildberries_monitor_prices.repositories;

import wildberries_monitor_prices.entity.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ICartRepository extends JpaRepository<LineItem, Long> {

}
