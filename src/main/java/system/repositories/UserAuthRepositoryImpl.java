package system.repositories;

import system.entities.UserAuth;

import javax.enterprise.context.RequestScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Optional;

@RequestScoped
public class UserAuthRepositoryImpl extends AbstractRepository<UserAuth, Long> implements UserAuthRepository  {

    public UserAuthRepositoryImpl() {
        super(UserAuth.class);
    }

    @Override
    public Optional<UserAuth> getByLogin(String login) {
        TypedQuery<UserAuth> getUserByLogin = createNamedQuery("UserAuth.getByLogin");
        getUserByLogin.setParameter("login", login);

        try {
            return Optional.of(getUserByLogin.getSingleResult());
        }
        catch(NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserAuth> getByToken(String token) {
        TypedQuery<UserAuth> getUserByToken = createNamedQuery("UserAuth.getByToken");
        getUserByToken.setParameter("token", token);

        try {
            return Optional.of(getUserByToken.getSingleResult());
        }
        catch(NoResultException e) {
            return Optional.empty();
        }
    }
}
