package investment;

import annotations.Service;
import java.math.BigDecimal;

@Service
public class InterestRateService {

    public BigDecimal currentMutualFundNav(Long id) {

        BigDecimal base = new BigDecimal("100");

        if (id % 2 == 0)
            return base.add(new BigDecimal("12.25"));
        else
            return base.add(new BigDecimal("7.40"));
    }
}
