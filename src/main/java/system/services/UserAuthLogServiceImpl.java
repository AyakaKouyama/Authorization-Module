package system.services;

import system.entities.UserAuthLog;
import system.exceptions.ApplicationException;
import system.repositories.UserAuthLogRepository;
import system.utils.SortOption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class UserAuthLogServiceImpl implements UserAuthLogService {

    @Inject
    UserAuthLogRepository userAuthLogRepository;

    @Override
    public boolean getUserAuthLogExistsById(Long id) {
        Optional<UserAuthLog> userAuthLog = userAuthLogRepository.getById(id);
        return  userAuthLog.isPresent();
    }

    @Override
    public UserAuthLog getUserAuthLogById(Long id) {
        return userAuthLogRepository.getById(id).orElseThrow(() -> new ApplicationException("User authentication log with given ID not found"));
    }

    @Override
    public UserAuthLog getUserAuthLogByLogin(String login) {
        return userAuthLogRepository.getByLogin(login).orElseThrow(() -> new ApplicationException("User authentication log with given login not found"));
    }

    @Override
    public List<UserAuthLog> getAllUserAuthLog() {
        return userAuthLogRepository.getAll();
    }

    @Override
    public List<UserAuthLog> getAllUsersAuthLog(String RSQLQuery, int page, int limit, List<SortOption> sortOptionList) {
        return userAuthLogRepository.getAll(RSQLQuery, page, limit, sortOptionList);
    }

    @Override
    public List<UserAuthLog> getAllLogsBetweenDates(LocalDateTime before, LocalDateTime after) {
        List<UserAuthLog> list = userAuthLogRepository.getAll();
        int size = list.size();

        for (int i=0; i<size; i++)
        {
            if(list.get(i).getGenerationDate().isBefore(before)) {
                list.remove(i);
            }
        }
        for (int i=0; i<size; i++)
        {
            if(list.get(i).getGenerationDate().isAfter(after)) {
                list.remove(i);
            }
        }
        return list;
    }

    @Override
    public List<UserAuthLog> getAllUserAuthLogsByLogin(String login) {
        List<UserAuthLog> userAuthLogsWithLogin = new ArrayList<>();
        List<UserAuthLog> allUserAuthLogs = getAllUserAuthLog();
        int size = allUserAuthLogs.size();

        for(int i = 0; i<size; i++){
            UserAuthLog current = allUserAuthLogs.get(i);
            if(current.getLogin().equals(login)){
                userAuthLogsWithLogin.add(current);
            }
        }

        return  userAuthLogsWithLogin;
    }

    @Override
    public void addUserAuthLog(UserAuthLog userAuth) {
        userAuthLogRepository.add(userAuth);
    }

    @Override
    public void deleteUserAuthLogById(Long id) {
        userAuthLogRepository.deleteByIdIfExists(id);
    }

    @Override
    public void deleteUserAuthLogByLogin(String login) {
        List<UserAuthLog> all = userAuthLogRepository.getAll();
        int size = all.size();

        for(int i = 0; i<size; i++){
            if(all.get(i).getLogin().equals(login)){
                userAuthLogRepository.deleteByIdIfExists(all.get(i).getId());
            }
        }
    }


    @Override
    public void deleteAll() {
        List<UserAuthLog> all = userAuthLogRepository.getAll();
        int size = all.size();
        for (int i = 0; i < size; i++) {
            Long id = all.get(i).getId();
            userAuthLogRepository.deleteByIdIfExists(id);
        }
    }
}
