package audit;

import annotations.Service;
import account.Account;

@Service
public class AuditService {

    public void asyncAudit(Account acc) {
        new Thread(() -> {
            try { Thread.sleep(20); } catch (Exception ignored) {}
            System.out.println("AUDIT LOG -> Account " + acc.getId() +
                    " : balance=" + acc.getBalance());
        }).start();
    }
}
