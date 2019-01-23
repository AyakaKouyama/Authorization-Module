package system.services;

import system.entities.IpLimiter;
import system.repositories.IpLimiterRepository;
import system.exceptions.ApplicationException;
import system.utils.SortOption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.swing.text.html.Option;

@Stateless
public class IpLimiterServiceImpl implements IpLimiterService
{
    @Inject
    IpLimiterRepository ipLimiterRepository;

    @Override
    public boolean getIpLimiterExistsByIp(String ip) {
        Optional<IpLimiter> ipLimiter = ipLimiterRepository.getByIp(ip);
        return  ipLimiter.isPresent();
    }

    @Override
    public IpLimiter getIpLimiterById(Long id) {
        return ipLimiterRepository.getById(id).orElseThrow(() -> new ApplicationException("IpLimiter with given ID not found."));
    }

    @Override
    public IpLimiter getIpLimiterByIp(String ip) {
        return ipLimiterRepository.getByIp(ip).orElseThrow(() -> new ApplicationException("IpLimiter with given IP not found."));
    }

    @Override
    public IpLimiter getIpLimiterByCounter(int counter) {
        return ipLimiterRepository.getByCounter(counter).orElseThrow(() -> new ApplicationException("IpLimiter with given IP counter not found."));
    }

    @Override
    public List<IpLimiter> getAllIpLimiters() {
        return ipLimiterRepository.getAll();
    }

    @Override
    public List<IpLimiter> getAllIpLimiters(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList) {
        return ipLimiterRepository.getAll(RSQLQuery, page, limit, sortOptionList);
    }

    @Override
    public void addIpLimiter(IpLimiter ipLimiter) {
        ipLimiterRepository.add(ipLimiter);
    }

    @Override
    public void deleteAll() {
        List<IpLimiter> all = ipLimiterRepository.getAll();
        int size = all.size();
        for (int i = 0; i < size; i++) {
            Long id = all.get(i).getId();
            ipLimiterRepository.deleteByIdIfExists(id);
        }
    }

    @Override
    public void deleteByIp(String ip) {
        Optional<IpLimiter> optIpLimiter = ipLimiterRepository.getByIp(ip);
        if(optIpLimiter.isPresent()){
        ipLimiterRepository.deleteByIdIfExists(optIpLimiter.get().getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        ipLimiterRepository.deleteByIdIfExists(id);
    }

    @Override
    public void decrementCounterByIp(String ip) {
        IpLimiter ipLimiter = getIpLimiterByIp(ip);
        int counter = ipLimiter.getIpCounter();
        ipLimiter.setIpCounter(counter - 1);
    }

    @Override
    public void incrementCounterByIp(String ip) {
        IpLimiter ipLimiter = getIpLimiterByIp(ip);
        int counter = ipLimiter.getIpCounter();
        ipLimiter.setIpCounter(counter + 1);
    }

    @Override
    public void resetCounterByIp(String ip) {
        getIpLimiterByIp(ip).setIpCounter(0);
    }

    @Override
    public void blockIpToByIp(String ip, LocalDateTime date) {
        getIpLimiterByIp(ip).setBlockedTo(date);
    }

    @Override
    public void unblockIp(String ip) {
        getIpLimiterByIp(ip).setBlockedTo(null);
    }
}
