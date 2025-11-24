package account;

import java.math.BigDecimal;

public class Account {
    private Long id;
    private BigDecimal balance;
    private boolean locked;

    public Account(Long id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
        this.locked = false;
    }

    public Long getId() { return id; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal b) { balance = b; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean b) { locked = b; }
}
