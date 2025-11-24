package investment;

import annotations.RestController;
import annotations.PostMapping;
import annotations.GetMapping;

import static spark.Spark.*;
import com.google.gson.Gson;

@RestController
public class InvestmentController {

    private static Gson gson = new Gson();

    public static void init(MutualFundService service) {

        post("/api/investment/rebalance", (req, res) -> {
            RebalanceReq b = gson.fromJson(req.body(), RebalanceReq.class);
            service.rebalance(b.id);
            return "{ \"status\": \"REBALANCED\" }";
        });

        get("/api/investment/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Portfolio p = service.getRepo().find(id);
            return gson.toJson(p);
        });
    }

    static class RebalanceReq {
        Long id;
    }
}
