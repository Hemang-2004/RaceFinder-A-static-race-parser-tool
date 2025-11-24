package fraud;

public class FraudScoringEngine {

    private int lastScore = 0;

    public int riskScore(Long id, double amount) {

        lastScore = (int)(Math.random() * 100);

        if (amount > 10000) lastScore += 20;
        if (id % 3 == 0) lastScore += 10;

        return lastScore;
    }
}
