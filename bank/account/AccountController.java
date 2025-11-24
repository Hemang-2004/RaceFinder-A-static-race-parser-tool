package account;

import annotations.RestController;
import annotations.PostMapping;
import annotations.GetMapping;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.math.BigDecimal;

@RestController
public class AccountController {

    private static Gson gson = new Gson();

    public static void init(AccountService service) {

        post("/api/account/deposit", (req, res) -> {
            var body = gson.fromJson(req.body(), DepositReq.class);
            service.deposit(body.id, new BigDecimal(body.amount));
            return "{ \"status\": \"OK\" }";
        });

        post("/api/account/transfer", (req, res) -> {
            var body = gson.fromJson(req.body(), TransferReq.class);
            service.transferAsync(
                body.fromId, body.toId,
                new BigDecimal(body.amount)
            );
            return "{ \"status\": \"TRANSFER_STARTED\" }";
        });

        get("/api/account/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Account acc = service.getRepo().findById(id).orElse(null);
            return gson.toJson(acc);
        });
    }

    static class DepositReq { Long id; String amount; }
    static class TransferReq { Long fromId; Long toId; String amount; }
}
