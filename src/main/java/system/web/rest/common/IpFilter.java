package system.web.rest.common;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import javax.inject.Inject;

import system.entities.IpLimiter;
import system.services.IpLimiterService;
@Limited
@Provider
public class IpFilter implements ContainerRequestFilter {

    @Inject
    private RequestDetails requestDetails;

    @Inject
    private ResponseMaker responseMaker;

    @Inject
    private IpLimiterService ipLimiterService;

    @Override
    public void filter(ContainerRequestContext containerRequestContext){

        String ipAddress = requestDetails.getRemoteAddress();

        if(!ipLimiterService.getIpLimiterExistsByIp(ipAddress)){
            IpLimiter ipLimiter = new IpLimiter();
            ipLimiter.setIp(ipAddress);
            ipLimiter.setIpCounter(0);
            ipLimiter.setBlockedTo(null);

            ipLimiterService.addIpLimiter(ipLimiter);
        }

        IpLimiter ipLimiter = ipLimiterService.getIpLimiterByIp(ipAddress);

        if(ipLimiter.getBlockedTo() != null){

            if(ipLimiter.getBlockedTo().isAfter(LocalDateTime.now())){
                containerRequestContext.abortWith(responseMaker.blockedError());
                return;
            }
            else{
                ipLimiterService.unblockIp(ipAddress);
                ipLimiterService.resetCounterByIp(ipAddress);
            }
        }

        ipLimiterService.incrementCounterByIp(ipAddress);

        if(ipLimiter.getIpCounter() > 5) {
            ipLimiterService.blockIpToByIp(ipAddress, LocalDateTime.now().plusMinutes(60));
            containerRequestContext.abortWith(responseMaker.blockedError());
        }
    }
}
