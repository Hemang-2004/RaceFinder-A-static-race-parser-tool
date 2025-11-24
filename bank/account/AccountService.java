package account;

import annotations.Service;
import java.math.BigDecimal;

@Service
public class AccountService {

    private AccountRepository repo = new AccountRepository();
    private LimitService limit = new LimitService();

    public void deposit(Long id, BigDecimal amount) {

        Account acc = repo.findById(id).orElse(null);
        if (acc == null) return;

        if (!limit.isDailyLimitExceeded(id, amount)) {

            BigDecimal bal = acc.getBalance();
            acc.setBalance(bal.add(amount));

            repo.save(acc);
        }
    }

    public void transferAsync(Long from, Long to, BigDecimal amount) {
        new Thread(() -> creditTransfer(from, to, amount)).start();
    }

    public void creditTransfer(Long from, Long to, BigDecimal amount) {

        Account a = repo.findById(from).orElse(null);
        Account b = repo.findById(to).orElse(null);

        if (a.getBalance().compareTo(amount) >= 0) {
            a.setBalance(a.getBalance().subtract(amount));
            b.setBalance(b.getBalance().add(amount));

            repo.save(a);
            repo.save(b);
        }
    }

    public AccountRepository getRepo() {
        return repo;
    }
}
