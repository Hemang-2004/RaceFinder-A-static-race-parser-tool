package account;

import annotations.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepository {

    private Map<Long, Account> map = new ConcurrentHashMap<>();

    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(map.get(id));
    }

    public Account save(Account a) {
        map.put(a.getId(), a);
        return a;
    }

    public void init(Long id, Account acc) {
        map.put(id, acc);
    }
}
