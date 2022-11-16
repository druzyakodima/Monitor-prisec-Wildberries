package wildberries_monitor_prices.entity.user;

import wildberries_monitor_prices.entity.LineItem;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 128, nullable = false)
    private Long chatId;

    @Column(length = 128)
    private String firstName;

    @Column(length = 128)
    private String lastName;

    @Column(length = 128)
    private String userName;

    @OneToMany(mappedBy = "user")
    private List<LineItem> lineItems;
    public User(Long id, Long chatId, String firstName, String lastName, String userName) {
        this.id = id;
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }

    public User(UserRepr userRepr) {
        this.chatId = userRepr.getChatId();
        this.firstName = userRepr.getFirstName();
        this.lastName = userRepr.getLastName();
        this.userName = userRepr.getUserName();
    }

    public User() {
    }

}
