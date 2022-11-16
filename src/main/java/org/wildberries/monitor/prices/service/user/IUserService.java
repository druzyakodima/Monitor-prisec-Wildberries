package org.wildberries.monitor.prices.service.user;

import org.wildberries.monitor.prices.entity.user.UserRepr;

import java.util.Optional;

public interface IUserService {

    void save(UserRepr userRepr);

    UserRepr findByChatId(Long id);

    Optional<UserRepr> findById(Long id);
}
