package investment;

import annotations.Service;
import java.math.BigDecimal;

@Service
public class MutualFundService {

    private InvestmentRepository repo = new InvestmentRepository();
    private InterestRateService nav = new InterestRateService();

    public void rebalance(Long id) {

        Portfolio p = repo.find(id);
        if (p == null) return;

        BigDecimal currentNav = nav.currentMutualFundNav(id);

        BigDecimal holding = p.get("FUND");

        BigDecimal newValue = holding.multiply(currentNav);

        p.update("FUND", newValue);

        repo.save(p);
    }

    public InvestmentRepository getRepo() {
        return repo;
    }
}
