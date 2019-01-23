package system.web.rest.am;

import com.andrew.modelmapper.core.ModelMapper;
import com.andrew.modelmapper.exceptions.ModelMapperException;
import system.entities.IpLimiter;
import system.entities.UserAuthLog;
import system.services.IpLimiterService;
import system.services.UserAuthLogService;
import system.utils.SortOption;
import system.web.dtos.IpLimiterDto;
import system.web.rest.common.EndpointHelper;
import system.web.rest.common.ResponseMaker;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.LocalDateTime;
import java.util.List;
import system.web.dtos.UserAuthLogDto;
import system.web.rest.common.Secured;
import system.entities.Role;

@RequestScoped
@Path("/am/auth")
public class AdminAuthEndpoint {

    @Context
    private SecurityContext securityContext;

    @Inject
    private EndpointHelper requestHelper;

    @Inject
    private ResponseMaker responseMaker;

    @Inject
    private UserAuthLogService userAuthLogService;

    @Inject
    private IpLimiterService ipLimiterService;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({Role.Name.Admin})
    @Path("/logs")
    public Response getAllAuthLogs(
            @DefaultValue("") @QueryParam("search") String query,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("0") @QueryParam("limit") int limit,
            @QueryParam("sort") List<String> sortParams)
            throws ModelMapperException {
        List<SortOption> sortOptionList = requestHelper.parseSortParams(sortParams);
        List<UserAuthLog> userAuthLogs = userAuthLogService.getAllUsersAuthLog(query, page, limit, sortOptionList);
        List<UserAuthLogDto> result = ModelMapper.mapEntityCollectionToDtoList(userAuthLogs, UserAuthLogDto.class);

        return responseMaker.successfulOperation(result);
    }

    @GET
    @Path("/logs/{login}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({Role.Name.Admin})
    public Response getAuthLogsByLogin(@PathParam("login") String login) throws ModelMapperException {
        List<UserAuthLog> userAuthLog = userAuthLogService.getAllUserAuthLogsByLogin(login);
        List<UserAuthLogDto> result = ModelMapper.mapEntityCollectionToDtoList(userAuthLog, UserAuthLogDto.class);
        return responseMaker.successfulOperation(result);
    }

    @DELETE
    @Path("/logs/id/{id}")
    @Secured({Role.Name.Admin})
    public Response deleteAuthLogById(@PathParam("id") Long id){
      userAuthLogService.deleteUserAuthLogById(id);
      return responseMaker.successfulOperation();
    }

    @DELETE
    @Path("/logs/login/{login}")
    @Secured({Role.Name.Admin})
    public Response deleteAuthLogByLogin(@PathParam("login") String login){
        userAuthLogService.deleteUserAuthLogByLogin(login);
        return responseMaker.successfulOperation();
    }

    @GET
    @Path("/ip_limiter")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({Role.Name.Admin})
    public Response getAllIpLimiters(
                                     @DefaultValue("") @QueryParam("search") String query,
                                     @DefaultValue("1") @QueryParam("page") int page,
                                     @DefaultValue("0") @QueryParam("limit") int limit,
                                     @QueryParam("sort") List<String> sortParams)
                                     throws ModelMapperException{

        List<SortOption> sortOptionList = requestHelper.parseSortParams(sortParams);
        List<IpLimiter> ipLimiters = ipLimiterService.getAllIpLimiters(query, page, limit, sortOptionList);
        List<IpLimiterDto> result = ModelMapper.mapEntityCollectionToDtoList(ipLimiters, IpLimiterDto.class);

        return responseMaker.successfulOperation(result);
    }

    @GET
    @Path("/ip_limiter/{ip}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({Role.Name.Admin})
    public Response getIpLimiterByIp(@PathParam("ip") String ip) throws ModelMapperException {
        IpLimiter ipLimiters;
        try{
            ipLimiters = ipLimiterService.getIpLimiterByIp(ip);
        }catch(Exception e){
            return responseMaker.notFoundError(e.getMessage());
        }

        IpLimiterDto result = ModelMapper.mapEntityToDto(ipLimiters, IpLimiterDto.class);
        return responseMaker.successfulOperation(result);
    }

    @DELETE
    @Path("/ip_limiter/ip/{ip}")
    @Secured({Role.Name.Admin})
    public Response deleteIpLimiterByIp(@PathParam("ip") String ip){
        ipLimiterService.deleteByIp(ip);
        return responseMaker.successfulOperation();
    }

    @DELETE
    @Path("/ip_limiter/id/{id}")
    @Secured({Role.Name.Admin})
    public Response deleteIpLimiterById(@PathParam("id") Long id) {
        ipLimiterService.deleteById(id);
        return responseMaker.successfulOperation();
    }

    @GET
    @Path("/ip_limiter/block/{ip}")
    @Secured({Role.Name.Admin})
    public Response blockIp(@PathParam("ip") String ip) {
        try {
            ipLimiterService.blockIpToByIp(ip, LocalDateTime.now().plusYears(100));
        }catch(Exception e){
            return  responseMaker.notFoundError(e.getMessage());
        }
        return responseMaker.successfulOperation();
    }

    @GET
    @Path("/ip_limiter/unblock/{ip}")
    @Secured({Role.Name.Admin})
    public Response unblockIp(@PathParam("ip") String ip){
        try {
            ipLimiterService.unblockIp(ip);
            ipLimiterService.resetCounterByIp(ip);
        }catch(Exception e){
            return  responseMaker.notFoundError(e.getMessage());
        }
        return responseMaker.successfulOperation();
    }
}