package wildberries_monitor_prices.service.user;

import wildberries_monitor_prices.entity.user.User;
import wildberries_monitor_prices.entity.user.UserRepr;
import wildberries_monitor_prices.repositories.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Data
@Service
public class UserService implements IUserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public void save(UserRepr userRepr) {

        User user = new User(userRepr);
        userRepository.save(user);
    }

    @Override
    public UserRepr findByChatId(Long chatId) {
        List<User> users = userRepository.findAll();

        if (!users.isEmpty()) {
            for (User user : users) {
                if (user.getChatId().equals(chatId)) {
                    return new UserRepr(user);
                }
            }
        }

        return null;
    }


    @Transactional
    @Override
    public Optional<UserRepr> findById(Long id) {
        return userRepository.findById(id).map(UserRepr::new);
    }

    public UserService() {
    }
}
