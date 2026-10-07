package com.investmentsentinel;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.investmentsentinel.domain.Investment;
import com.investmentsentinel.service.monitoring.DrawdownCalculation;

class InvestmentCalculationTest {

    @Test
    @DisplayName("Calendar-month logic adds exactly 3 calendar months, not merely 90 days")
    void testCalendarMonthReviewDate() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        LocalDate investmentDate = LocalDate.of(2026, 1, 10);
        inv.recordInvestment(new BigDecimal("50.00"), investmentDate, 3);

        // 10 January 2026 + 3 calendar months = 10 April 2026
        assertThat(inv.getNextReviewDate()).isEqualTo(LocalDate.of(2026, 4, 10));
    }

    @Test
    @DisplayName("Month-end adjustment: Jan 31 plus 3 months lands on April 30")
    void testMonthEndAdjustment() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        LocalDate jan31 = LocalDate.of(2026, 1, 31);
        inv.recordInvestment(new BigDecimal("50.00"), jan31, 3);

        // April has 30 days, so Jan 31 + 3 months = April 30
        assertThat(inv.getNextReviewDate()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    @DisplayName("Leap year handling: Nov 29 2023 plus 3 months lands on Feb 29 2024")
    void testLeapYearCalculation() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        LocalDate nov29 = LocalDate.of(2023, 11, 29);
        inv.recordInvestment(new BigDecimal("50.00"), nov29, 3);

        assertThat(inv.getNextReviewDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    @DisplayName("Non-leap year handling: Nov 29 2024 plus 3 months lands on Feb 28 2025")
    void testNonLeapYearCalculation() {
        Investment inv = new Investment("ATLAS GLOBAL", "USD");
        LocalDate nov29 = LocalDate.of(2024, 11, 29);
        inv.recordInvestment(new BigDecimal("50.00"), nov29, 3);

        assertThat(inv.getNextReviewDate()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Drawdown boundary test: -4.99% falls under normal $25 contribution")
    void testDrawdownJustAboveFivePercent() {
        // Reference = 10,000, Current = 9,501 => Drawdown = -4.99%
        BigDecimal reference = new BigDecimal("10000.00");
        BigDecimal current = new BigDecimal("9501.00");

        DrawdownCalculation calc = DrawdownCalculation.calculate(
                current, reference,
                new BigDecimal("25.00"),
                new BigDecimal("5.0"), new BigDecimal("50.00"),
                new BigDecimal("15.0"), new BigDecimal("75.00"),
                new BigDecimal("25.0"), new BigDecimal("100.00")
        );

        assertThat(calc.drawdownPercentage()).isEqualByComparingTo(new BigDecimal("-4.99"));
        assertThat(calc.suggestedAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("Drawdown boundary test: exactly -5.00% triggers $50 contribution")
    void testDrawdownExactlyFivePercent() {
        // Reference = 10,000, Current = 9,500 => Drawdown = -5.00%
        BigDecimal reference = new BigDecimal("10000.00");
        BigDecimal current = new BigDecimal("9500.00");

        DrawdownCalculation calc = DrawdownCalculation.calculate(
                current, reference,
                new BigDecimal("25.00"),
                new BigDecimal("5.0"), new BigDecimal("50.00"),
                new BigDecimal("15.0"), new BigDecimal("75.00"),
                new BigDecimal("25.0"), new BigDecimal("100.00")
        );

        assertThat(calc.drawdownPercentage()).isEqualByComparingTo(new BigDecimal("-5.00"));
        assertThat(calc.suggestedAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Drawdown boundary test: -14.99% remains at $50 contribution tier")
    void testDrawdownJustAboveFifteenPercent() {
        // Reference = 10,000, Current = 8,501 => Drawdown = -14.99%
        BigDecimal reference = new BigDecimal("10000.00");
        BigDecimal current = new BigDecimal("8501.00");

        DrawdownCalculation calc = DrawdownCalculation.calculate(
                current, reference,
                new BigDecimal("25.00"),
                new BigDecimal("5.0"), new BigDecimal("50.00"),
                new BigDecimal("15.0"), new BigDecimal("75.00"),
                new BigDecimal("25.0"), new BigDecimal("100.00")
        );

        assertThat(calc.drawdownPercentage()).isEqualByComparingTo(new BigDecimal("-14.99"));
        assertThat(calc.suggestedAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Drawdown boundary test: exactly -15.00% triggers $75 contribution")
    void testDrawdownExactlyFifteenPercent() {
        // Reference = 10,000, Current = 8,500 => Drawdown = -15.00%
        BigDecimal reference = new BigDecimal("10000.00");
        BigDecimal current = new BigDecimal("8500.00");

        DrawdownCalculation calc = DrawdownCalculation.calculate(
                current, reference,
                new BigDecimal("25.00"),
                new BigDecimal("5.0"), new BigDecimal("50.00"),
                new BigDecimal("15.0"), new BigDecimal("75.00"),
                new BigDecimal("25.0"), new BigDecimal("100.00")
        );

        assertThat(calc.drawdownPercentage()).isEqualByComparingTo(new BigDecimal("-15.00"));
        assertThat(calc.suggestedAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("Drawdown boundary test: exactly -25.00% triggers $100 contribution")
    void testDrawdownExactlyTwentyFivePercent() {
        // Reference = 10,000, Current = 7,500 => Drawdown = -25.00%
        BigDecimal reference = new BigDecimal("10000.00");
        BigDecimal current = new BigDecimal("7500.00");

        DrawdownCalculation calc = DrawdownCalculation.calculate(
                current, reference,
                new BigDecimal("25.00"),
                new BigDecimal("5.0"), new BigDecimal("50.00"),
                new BigDecimal("15.0"), new BigDecimal("75.00"),
                new BigDecimal("25.0"), new BigDecimal("100.00")
        );

        assertThat(calc.drawdownPercentage()).isEqualByComparingTo(new BigDecimal("-25.00"));
        assertThat(calc.suggestedAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
