package account;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public class DailyLimitCache {

    private ConcurrentHashMap<Long, BigDecimal> cache = new ConcurrentHashMap<>();

    public BigDecimal get(Long id) {
        return cache.getOrDefault(id, BigDecimal.ZERO);
    }

    public void put(Long id, BigDecimal amount) {
        cache.put(id, amount);
    }
}
