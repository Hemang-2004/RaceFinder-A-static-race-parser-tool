package loan;

import annotations.Service;

@Service
public class CreditScoreService {

    public int getScore(Long customerId) {
        int score = (int)(Math.random() * 300 + 300);

        if (customerId % 2 == 0) score += 50;
        if (customerId % 5 == 0) score -= 40;

        return Math.min(850, Math.max(300, score));
    }
}
