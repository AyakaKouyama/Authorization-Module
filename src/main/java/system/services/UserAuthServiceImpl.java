package system.services;

import system.entities.User;
import system.entities.UserAuth;
import system.exceptions.ApplicationException;
import system.repositories.UserAuthRepository;
import system.utils.Hash;
import system.utils.SortOption;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Stateless
public class UserAuthServiceImpl implements UserAuthService {

    @Inject
    UserAuthRepository userAuthRepository;

    @Inject
    UserService userService;

    @Override
    public boolean authenticate(String login, String password){
        try {
            User user = userService.getUserByLogin(login);
            String hashedPassword = Hash.sha512hash(password);
            return user.getPassword().equals(hashedPassword);
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public void revokeAuthentication(String login){
        deleteUserAuthByLogin(login);
    }

    @Override
    public String issueAuthTokenForUserWithLoginAndIp(String login, String ip) {

        SecureRandom random = new SecureRandom();
        String randomString = new BigInteger(130, random).toString(32);
        LocalDateTime date = LocalDateTime.now();

        String token = Hash.sha512hash(randomString) + Hash.sha512hash(date) + '$' + (login.length() % 10);

        LocalDateTime expirationDate = date.plusHours(3);
        UserAuth userAuth = new UserAuth();
        userAuth.setLogin(login);
        userAuth.setToken(token);
        userAuth.setTokenExpirationDate(expirationDate);
        userAuth.setIp(ip);
        addUserAuth(userAuth);

        return token;
    }

    @Override
    public boolean getUserAuthExistsById(Long id) {
        Optional<UserAuth> userAuth = userAuthRepository.getById(id);
        return userAuth.isPresent();
    }

    @Override
    public UserAuth getUserAuthById(Long id) {
        return userAuthRepository.getById(id).orElseThrow(() -> new ApplicationException("There is no user authorization with given user ID."));
    }

    @Override
    public UserAuth getUserAuthByLogin(String login) {
        return userAuthRepository.getByLogin(login).orElseThrow(() -> new ApplicationException("There is no user authorization with given login"));
    }

    @Override
    public UserAuth getUserAuthByToken(String token) {
        return userAuthRepository.getByToken(token).orElseThrow(() -> new ApplicationException("There is no user authorization with given token"));
    }

    @Override
    public Optional<UserAuth> getOptionalUserAuthByToken(String token){
        return  userAuthRepository.getByToken(token);
    }

    @Override
    public List<UserAuth> getAllUserAuth() {
        return userAuthRepository.getAll();
    }

    @Override
    public List<UserAuth> getAllUsersAuth(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList) {
        return userAuthRepository.getAll(RSQLQuery, page, limit, sortOptionList);
    }

    @Override
    public void addUserAuth(UserAuth userAuth) {
        userAuthRepository.add(userAuth);
    }

    @Override
    public void deleteUserAuthById(Long id) {
        userAuthRepository.deleteByIdIfExists(id);
    }

    @Override
    public void deleteUserAuthByLogin(String login) {
        Optional<UserAuth> userAuth = userAuthRepository.getByLogin(login);
        if(userAuth.isPresent()){
            userAuthRepository.deleteByIdIfExists(userAuth.get().getId());
        }
    }

    @Override
    public void deleteAll() {
        List<UserAuth> all = userAuthRepository.getAll();
        int size = all.size();
        for (int i = 0; i < size; i++) {
            Long id = all.get(i).getId();
            userAuthRepository.deleteByIdIfExists(id);
        }
    }
}
