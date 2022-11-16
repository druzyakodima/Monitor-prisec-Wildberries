package wildberries_monitor_prices.service.user;

import wildberries_monitor_prices.entity.user.UserRepr;

import java.util.Optional;

public interface IUserService {

    void save(UserRepr userRepr);

    UserRepr findByChatId(Long id);

    Optional<UserRepr> findById(Long id);
}
