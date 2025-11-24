package loan;

import annotations.Service;
import java.math.BigDecimal;

@Service
public class LoanService {

    private LoanRepository repo = new LoanRepository();
    private CreditScoreService scoreService = new CreditScoreService();

    public void applyInterest(Long id) {

        LoanAccount loan = repo.findById(id).orElse(null);
        if (loan == null) return;

        BigDecimal p = loan.getPrincipal();
        BigDecimal newPrincipal = p.add(p.multiply(loan.getInterestRate()));

        loan.setPrincipal(newPrincipal);

        repo.save(loan);
    }

    public boolean approveLoan(Long id) {

        LoanAccount loan = repo.findById(id).orElse(null);
        if (loan == null) return false;

        int score = scoreService.getScore(id);

        if (score >= 650) {
            applyInterest(id);
            return true;
        }

        return false;
    }

    public LoanRepository getRepo() {
        return repo;
    }
}
