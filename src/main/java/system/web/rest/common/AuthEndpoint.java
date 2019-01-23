package system.web.rest.common;

import system.web.dtos.Credentials;
import system.entities.UserAuthLog;
import system.services.IpLimiterService;
import system.services.UserAuthLogService;
import system.services.UserAuthService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@RequestScoped
@Path("/auth")
public class AuthEndpoint
{
    @Inject
    UserAuthService authService;

    @Inject
    UserAuthLogService userAuthLogService;

    @Inject
    IpLimiterService ipLimiterService;

    @Inject
    RequestDetails requestDetails;

    @Inject
    ResponseMaker responseMaker;

    @Context
    SecurityContext securityContext;

    @POST
    @Limited
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(Credentials credentials) {
        if(credentials == null){
            return  responseMaker.notFoundError();
        }
        boolean result = authService.authenticate(credentials.getLogin(), credentials.getPassword());
        UserAuthLog userAuthLog = new UserAuthLog();
        userAuthLog.setLogin(credentials.getLogin());
        userAuthLog.setGenerationDate(LocalDateTime.now());

        if(result) {
           String ip = requestDetails.getRemoteAddress();
           String token = authService.issueAuthTokenForUserWithLoginAndIp(credentials.getLogin(), ip);

           userAuthLog.setDescription("Successful operation.");
           userAuthLogService.addUserAuthLog(userAuthLog);

           ipLimiterService.decrementCounterByIp(ip);
           return responseMaker.successfulOperation(token);
        }

        userAuthLog.setDescription("Failed to authenticate.");
        userAuthLogService.addUserAuthLog(userAuthLog);
        return responseMaker.notFoundError();
    }

    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserAuth() {
        String login = securityContext.getUserPrincipal().getName();
        return responseMaker.successfulOperation(login);
    }

    @DELETE
    @Secured
    public Response revokeAuthentication() {
        String login = securityContext.getUserPrincipal().getName();
        authService.revokeAuthentication(login);

        UserAuthLog userAuthLog = new UserAuthLog();
        userAuthLog.setLogin(login);
        userAuthLog.setGenerationDate(LocalDateTime.now());
        userAuthLog.setDescription("Authentication has been removed.");
        userAuthLogService.addUserAuthLog(userAuthLog);

        return responseMaker.successfulOperation();
    }
}
