package system.services;

import system.entities.UserAuthLog;
import system.utils.SortOption;

import java.time.LocalDateTime;
import java.util.List;

public interface UserAuthLogService {

    boolean getUserAuthLogExistsById(Long id);
    UserAuthLog getUserAuthLogById(Long id);
    UserAuthLog getUserAuthLogByLogin(String login);
    List<UserAuthLog> getAllUserAuthLog();
    List<UserAuthLog> getAllUsersAuthLog(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList);
    List<UserAuthLog> getAllLogsBetweenDates(LocalDateTime before, LocalDateTime after);
    List<UserAuthLog> getAllUserAuthLogsByLogin(String login);
    void addUserAuthLog(UserAuthLog userAuth);
    void deleteUserAuthLogById(Long id);
    void deleteUserAuthLogByLogin(String login);
    void deleteAll();
}
