package com.example.botwithallimportantfunc.repositories;

import com.example.botwithallimportantfunc.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
