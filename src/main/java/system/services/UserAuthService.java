package system.services;

import system.entities.UserAuth;
import system.utils.SortOption;

import java.util.List;
import java.util.Optional;

public interface UserAuthService
{
    boolean authenticate(String login, String password);
    void revokeAuthentication(String login);
    String issueAuthTokenForUserWithLoginAndIp(String login, String ip);
    boolean getUserAuthExistsById(Long id);
    UserAuth getUserAuthById(Long id);
    UserAuth getUserAuthByLogin(String login);
    UserAuth getUserAuthByToken(String token);
    Optional<UserAuth> getOptionalUserAuthByToken(String token);
    List<UserAuth> getAllUserAuth();
    List<UserAuth> getAllUsersAuth(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList);
    void addUserAuth(UserAuth userAuth);
    void deleteUserAuthById(Long id);
    void deleteUserAuthByLogin(String login);
    void deleteAll();
}
