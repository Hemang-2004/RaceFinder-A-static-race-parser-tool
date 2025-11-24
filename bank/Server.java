import static spark.Spark.*;

import account.*;
import loan.*;
import investment.*;
import payment.*;
import audit.*;

public class Server {

    public static void main(String[] args) {

        port(8080);

        AccountService accService = new AccountService();
        LoanService loanService = new LoanService();
        MutualFundService invService = new MutualFundService();
        PaymentService payService = new PaymentService();
        AuditService auditService = new AuditService();

        // initialize endpoints
        account.AccountController.init(accService);
        loan.LoanController.init(loanService);
        investment.InvestmentController.init(invService);
        payment.PaymentController.init(payService);
        audit.AuditController.init(auditService);

        System.out.println("🚀 Backend running on http://localhost:8080");
    }
}
