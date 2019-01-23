package system.repositories;

import system.entities.UserAuthLog;
import java.util.Optional;

public interface UserAuthLogRepository extends Repository<UserAuthLog, Long> {

    Optional<UserAuthLog> getByLogin(String login);
}