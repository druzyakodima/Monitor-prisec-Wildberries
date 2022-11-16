package org.wildberries.monitor.prices.repositories;

import org.wildberries.monitor.prices.entity.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ICartRepository extends JpaRepository<LineItem, Long> {

}
