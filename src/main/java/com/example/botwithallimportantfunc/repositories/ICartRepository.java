package com.example.botwithallimportantfunc.repositories;

import com.example.botwithallimportantfunc.entity.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ICartRepository extends JpaRepository<LineItem, Long> {

}
