package fraud;

import annotations.Service;

@Service
public class FraudService {

    private FraudScoringEngine engine = new FraudScoringEngine();

    public boolean isFraudulent(Long id, double amount) {
        int score = engine.riskScore(id, amount);
        return score > 70;
    }

    public FraudScoringEngine getEngine() { return engine; }
}
