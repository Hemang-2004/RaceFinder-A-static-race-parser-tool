package loan;

import java.math.BigDecimal;

public class LoanAccount {
    private Long id;
    private BigDecimal principal;
    private BigDecimal interestRate;

    public LoanAccount(Long id, BigDecimal principal, BigDecimal rate) {
        this.id = id;
        this.principal = principal;
        this.interestRate = rate;
    }

    public Long getId() { return id; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal p) { principal = p; }

    public BigDecimal getInterestRate() { return interestRate; }
}
