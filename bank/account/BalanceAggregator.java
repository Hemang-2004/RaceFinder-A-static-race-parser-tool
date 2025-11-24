package account;

import java.math.BigDecimal;
import java.util.List;

public class BalanceAggregator {

    public BigDecimal sumBalances(List<Account> accounts) {
        BigDecimal sum = BigDecimal.ZERO;

        for (Account acc : accounts) {
            sum = sum.add(acc.getBalance());
        }

        return sum;
    }
}
