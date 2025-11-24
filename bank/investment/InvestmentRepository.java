package investment;

import annotations.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

@Repository
public class InvestmentRepository {

    private ConcurrentHashMap<Long, Portfolio> map = new ConcurrentHashMap<>();

    public Portfolio find(Long id) {
        return map.get(id);
    }

    public void save(Portfolio p) {
        map.put(p.id, p);
    }

    public void init(Long id, Portfolio p) {
        map.put(id, p);
    }
}
