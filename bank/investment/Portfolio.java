package investment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Portfolio {

    public Long id;
    public Map<String, BigDecimal> holdings = new HashMap<>();

    public Portfolio(Long id) {
        this.id = id;
    }

    public BigDecimal get(String ticker) {
        return holdings.getOrDefault(ticker, BigDecimal.ZERO);
    }

    public void update(String ticker, BigDecimal value) {
        holdings.put(ticker, value);
    }
}
