package ru.max.codereviewbot.persist;

import org.springframework.stereotype.Repository;
import ru.max.codereviewbot.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    public void save(User user) {
        users.put(user.id(), user);
    }

    public Optional<User> get(long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public List<User> findAllByIds(Set<Long> ids) {
        return users
            .values()
            .stream()
            .filter(user -> ids.contains(user.id()))
            .toList();
    }
}
