package payment;

import annotations.Service;
import account.*;
import fraud.FraudService;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private FraudService fraud = new FraudService();
    private AccountRepository repo = new AccountRepository();

    public boolean pay(Long id, BigDecimal amount) {

        Account acc = repo.findById(id).orElse(null);
        if (acc == null) return false;

        if (fraud.isFraudulent(id, amount.doubleValue()))
            return false;

        if (acc.getBalance().compareTo(amount) >= 0) {

            acc.setBalance(acc.getBalance().subtract(amount));

            repo.save(acc);
            return true;
        }

        return false;
    }

    public AccountRepository getRepo() { return repo; }
}
