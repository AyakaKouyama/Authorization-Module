package system.repositories;

import system.entities.IpLimiter;
import java.util.Optional;

public interface IpLimiterRepository extends Repository<IpLimiter, Long> {
    Optional<IpLimiter> getByIp(String ip);
    Optional<IpLimiter> getByCounter(int counter);
}
