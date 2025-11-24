package payment;

import annotations.RestController;
import annotations.PostMapping;
import annotations.GetMapping;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.math.BigDecimal;

@RestController
public class PaymentController {

    private static Gson gson = new Gson();

    public static void init(PaymentService service) {

        post("/api/payment/pay", (req, res) -> {
            PayReq b = gson.fromJson(req.body(), PayReq.class);
            boolean ok = service.pay(b.id, new BigDecimal(b.amount));
            return "{ \"success\": " + ok + " }";
        });

        get("/api/payment/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            return gson.toJson(service.getRepo().findById(id).orElse(null));
        });
    }

    static class PayReq {
        Long id;
        String amount;
    }
}
