package system.repositories;

import system.entities.IpLimiter;

import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@RequestScoped
public class IpLimiterRepositoryImpl extends AbstractRepository<IpLimiter, Long> implements IpLimiterRepository {

    public IpLimiterRepositoryImpl() {
        super(IpLimiter.class);
    }

    @Override
    public Optional<IpLimiter> getByIp(String ip) {
        TypedQuery<IpLimiter> getByIp = createNamedQuery("IpLimiter.getByIp");
        getByIp.setParameter("ip", ip);

        try {
            return Optional.of(getByIp.getSingleResult());
        }
        catch(NoResultException e) {
            return Optional.empty();
        }

    }

    @Override
    public Optional<IpLimiter> getByCounter(int counter) {
        TypedQuery<IpLimiter> getByCounter = createNamedQuery("IpLimiter.getByCounter");
        getByCounter.setParameter("counter", counter);

        try {
            return Optional.of(getByCounter.getSingleResult());
        }
        catch(NoResultException e) {
            return Optional.empty();
        }

    }
}
