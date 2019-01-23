package system.services;

import system.entities.IpLimiter;
import system.utils.SortOption;

import java.time.LocalDateTime;
import java.util.List;

public interface IpLimiterService {
    boolean getIpLimiterExistsByIp(String ip);
    IpLimiter getIpLimiterById(Long id);
    IpLimiter getIpLimiterByIp(String ip);
    IpLimiter getIpLimiterByCounter(int counter);
    List<IpLimiter> getAllIpLimiters();
    List<IpLimiter> getAllIpLimiters(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList);
    void addIpLimiter(IpLimiter ipLimiter);
    void deleteAll();
    void deleteByIp(String ip);
    void deleteById(Long id);
    void decrementCounterByIp(String ip);
    void incrementCounterByIp(String ip);
    void resetCounterByIp(String ip);
    void blockIpToByIp(String ip, LocalDateTime date);
    void unblockIp(String ip);
}
