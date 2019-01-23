package system.repositories;

import system.entities.UserAuth;
import java.util.Optional;

public interface UserAuthRepository extends Repository<UserAuth, Long> {
    Optional<UserAuth> getByLogin(String login);
    Optional<UserAuth> getByToken(String token);
}
