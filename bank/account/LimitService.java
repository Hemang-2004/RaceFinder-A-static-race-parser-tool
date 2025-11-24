package account;

import annotations.Service;
import java.math.BigDecimal;

@Service
public class LimitService {

    private DailyLimitCache dailyLimitCache = new DailyLimitCache();

    public boolean isDailyLimitExceeded(Long id, BigDecimal amount) {
        BigDecimal used = dailyLimitCache.get(id);

        if (used.add(amount).compareTo(new BigDecimal("50000")) > 0) {
            return true;
        }

        dailyLimitCache.put(id, used.add(amount));
        return false;
    }
}
