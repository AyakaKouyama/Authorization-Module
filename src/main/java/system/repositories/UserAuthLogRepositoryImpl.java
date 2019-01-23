package system.repositories;

import system.entities.UserAuthLog;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@RequestScoped
public class UserAuthLogRepositoryImpl extends AbstractRepository<UserAuthLog, Long> implements UserAuthLogRepository {

    public UserAuthLogRepositoryImpl() {
        super(UserAuthLog.class);
    }

    @Override
    public Optional<UserAuthLog> getByLogin(String login) {
        TypedQuery<UserAuthLog> getUserAuthLogByLogin = createNamedQuery("UserAuthLog.getByLogin");
        getUserAuthLogByLogin.setParameter("login", login);

        try {
            return Optional.of(getUserAuthLogByLogin.getSingleResult());
        }
        catch(NoResultException e) {
            return Optional.empty();
        }
    }
}
