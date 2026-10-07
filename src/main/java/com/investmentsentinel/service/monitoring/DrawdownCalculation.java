package com.investmentsentinel.service.monitoring;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record DrawdownCalculation(
        BigDecimal currentValue,
        BigDecimal referenceValue,
        BigDecimal drawdownPercentage, // e.g. -6.92
        BigDecimal suggestedAmount,    // e.g. 50.00
        BigDecimal activeThreshold,    // e.g. -5.00, -15.00, -25.00 or 0.00
        String reason
) {
    public static DrawdownCalculation calculate(BigDecimal current, BigDecimal reference,
                                                BigDecimal normalAmount,
                                                BigDecimal threshold1, BigDecimal amount1,
                                                BigDecimal threshold2, BigDecimal amount2,
                                                BigDecimal threshold3, BigDecimal amount3) {
        if (reference == null || reference.compareTo(BigDecimal.ZERO) <= 0) {
            return new DrawdownCalculation(current, reference, BigDecimal.ZERO, normalAmount, BigDecimal.ZERO, "Reference value is zero or unset");
        }

        // drawdown = ((current - reference) / reference) * 100
        BigDecimal diff = current.subtract(reference);
        BigDecimal drawdown = diff.divide(reference, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // Thresholds are configured as positive numbers (e.g. 5.0, 15.0, 25.0), representing drops (-5%, -15%, -25%)
        BigDecimal t1 = threshold1.negate();
        BigDecimal t2 = threshold2.negate();
        BigDecimal t3 = threshold3.negate();

        BigDecimal suggestedAmount;
        BigDecimal activeThreshold;
        String reason;

        if (drawdown.compareTo(t3) <= 0) {
            suggestedAmount = amount3;
            activeThreshold = t3;
            reason = "Market is ≥" + threshold3 + "% below reference";
        } else if (drawdown.compareTo(t2) <= 0) {
            suggestedAmount = amount2;
            activeThreshold = t2;
            reason = "Market is ≥" + threshold2 + "% below reference";
        } else if (drawdown.compareTo(t1) <= 0) {
            suggestedAmount = amount1;
            activeThreshold = t1;
            reason = "Market is ≥" + threshold1 + "% below reference";
        } else {
            suggestedAmount = normalAmount;
            activeThreshold = BigDecimal.ZERO;
            reason = "Market drawdown is within normal bounds (above -" + threshold1 + "%)";
        }

        return new DrawdownCalculation(current, reference, drawdown, suggestedAmount, activeThreshold, reason);
    }
}
