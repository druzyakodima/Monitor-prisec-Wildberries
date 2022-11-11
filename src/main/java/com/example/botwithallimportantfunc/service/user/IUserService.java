package com.example.botwithallimportantfunc.service.user;

import com.example.botwithallimportantfunc.entity.user.UserRepr;

import java.util.Optional;

public interface IUserService {

    void save(UserRepr userRepr);

    UserRepr findByChatId(Long id);

    Optional<UserRepr> findById(Long id);
}
