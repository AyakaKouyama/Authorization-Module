package system.web.rest.common;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.HttpHeaders;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;

import system.services.UserService;
import system.services.UserAuthService;
import system.services.UserRoleService;
import system.entities.UserAuth;
import system.entities.Role;

@Secured
@Provider
public class SecuredFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private RequestDetails requestDetails;

    @Inject
    private ResponseMaker responseMaker;

    @Inject
    private UserAuthService userAuthService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;

    @Override
    public void filter(ContainerRequestContext containerRequestContext){

        String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if(!isTokenBasedAuthentication(authorizationHeader)){
            containerRequestContext.abortWith(responseMaker.authenticationError());
            return;
        }

        String authToken = authorizationHeader.substring(RestConfig.AUTH_SCHEME.length()).trim();
        Optional<UserAuth> userAuthOptional = userAuthService.getOptionalUserAuthByToken(authToken);

        if(!userAuthOptional.isPresent()){
            containerRequestContext.abortWith(responseMaker.authenticationError());
            return;
        }

        UserAuth userAuth = userAuthOptional.get();

        if(userAuth.getTokenExpirationDate().isBefore(LocalDateTime.now())) {
            userAuthService.deleteUserAuthByLogin(userAuth.getLogin());
            containerRequestContext.abortWith(responseMaker.authenticationError());
            return;
        }

        if(!userAuth.getIp().equals(requestDetails.getRemoteAddress())){
            containerRequestContext.abortWith(responseMaker.authenticationError());
            return;
        }

        Method resourceMethod = resourceInfo.getResourceMethod();

        List<Role.Name> currentMethodRoles = new ArrayList<>();

        Secured secured = resourceMethod.getAnnotation(Secured.class);
        if(secured != null) {
            currentMethodRoles = Arrays.asList(secured.value());
        }

        Long id = userService.getUserByLogin(userAuth.getLogin()).getId();
        List<Role> userRoles = userRoleService.getUserRolesByUserId(id);
        List<Role.Name> userRoleNames = new ArrayList<>();

        for(int i = 0; i<userRoles.size(); i++){
            userRoleNames.add(userRoles.get(i).getName());
        }

        if(!hasOneOfRoles(currentMethodRoles, userRoleNames)){
            containerRequestContext.abortWith(responseMaker.authorizationError());
            return;
        }

        final SecurityContext currentSecurityContext = containerRequestContext.getSecurityContext();
        containerRequestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return userAuth::getLogin;
            }

            @Override
            public boolean isUserInRole(String roleName) {
                Role.Name role;

                try{
                    role = Role.Name.valueOf(roleName);
                }catch(Exception e){
                    return  false;
                }

                return userRoleNames.contains(role);
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return RestConfig.AUTH_SCHEME;
            }
        });
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase().
                startsWith(RestConfig.AUTH_SCHEME.toLowerCase() + " ");
    }

    private boolean hasOneOfRoles(List<Role.Name> allRoles, List<Role.Name> userRoles){

        for(int i = 0; i<allRoles.size(); i++){
            if(userRoles.contains(allRoles.get(i))){
                return true;
            }
        }
        return  false;
    }
}
