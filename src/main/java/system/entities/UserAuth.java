package system.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "\"user_auth\"")
@NamedQueries({
        @NamedQuery(name = "UserAuth.getByLogin",
                query = "SELECT u FROM UserAuth u WHERE u.login = :login"),
        @NamedQuery(name = "UserAuth.getByToken",
                query = "SELECT u FROM UserAuth u WHERE u.token = :token")
})
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "login", length = 64, nullable = false)
    private String login;

    @Column(name = "token", length = 512, nullable = false)
    private String token;

    @Column(name = "token_expiration_date", nullable = false)
    private LocalDateTime tokenExpirationDate;

    @Column(name = "ip", nullable = false)
    private String ip;

    public UserAuth() {
        //default public constructor
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getTokenExpirationDate() {
		return tokenExpirationDate;
	}

	public void setTokenExpirationDate(LocalDateTime tokenExpirationDate) { this.tokenExpirationDate = tokenExpirationDate; }

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserAuth)) return false;
		UserAuth userAuth = (UserAuth) o;
		return Objects.equals(getId(), userAuth.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
