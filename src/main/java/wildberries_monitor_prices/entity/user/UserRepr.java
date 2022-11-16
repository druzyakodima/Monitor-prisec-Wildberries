package wildberries_monitor_prices.entity.user;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;

@Data
public class UserRepr {

    private Long id;

    @Nullable
    private Long chatId;

    private String firstName;

    private String lastName;

    @NotEmpty
    private String userName;

    public UserRepr(Long id, @Nullable Long chatId, String firstName, String lastName, String userName) {
        this.id = id;
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }

    public UserRepr(@Nullable Long chatId, String firstName, String lastName, String userName) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }

    public UserRepr(User user) {
        this.id = user.getId();
        this.chatId = user.getChatId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
    }



    public UserRepr() {
    }
}
