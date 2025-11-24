package loan;

import annotations.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

@Repository
public class LoanRepository {

    private Map<Long, LoanAccount> map = new ConcurrentHashMap<>();

    public Optional<LoanAccount> findById(Long id) {
        return Optional.ofNullable(map.get(id));
    }

    public LoanAccount save(LoanAccount loan) {
        map.put(loan.getId(), loan);
        return loan;
    }

    public void init(Long id, LoanAccount l) {
        map.put(id, l);
    }
}
