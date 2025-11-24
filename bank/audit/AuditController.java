package audit;

import annotations.RestController;
import annotations.PostMapping;

import static spark.Spark.*;
import com.google.gson.Gson;

@RestController
public class AuditController {

    private static Gson gson = new Gson();

    public static void init(AuditService service) {

        post("/api/audit/log", (req, res) -> {
            AuditReq b = gson.fromJson(req.body(), AuditReq.class);
            service.asyncAudit(new account.Account(b.id, b.balance));
            return "{ \"audit\": \"queued\" }";
        });
    }

    static class AuditReq { Long id; java.math.BigDecimal balance; }
}
