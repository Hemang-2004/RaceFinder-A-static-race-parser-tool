package loan;

import annotations.RestController;
import annotations.PostMapping;
import annotations.GetMapping;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.math.BigDecimal;

@RestController
public class LoanController {

    private static Gson gson = new Gson();

    public static void init(LoanService service) {

        post("/api/loan/apply-interest", (req, res) -> {
            var body = gson.fromJson(req.body(), ApplyReq.class);
            service.applyInterest(body.id);
            return "{ \"status\": \"APPLIED\" }";
        });

        post("/api/loan/approve", (req, res) -> {
            var body = gson.fromJson(req.body(), ApproveReq.class);
            boolean ok = service.approveLoan(body.id);
            return "{ \"approved\": " + ok + " }";
        });

        get("/api/loan/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            LoanAccount la = service.getRepo().findById(id).orElse(null);
            return gson.toJson(la);
        });
    }

    static class ApplyReq { Long id; }
    static class ApproveReq { Long id; }
}
