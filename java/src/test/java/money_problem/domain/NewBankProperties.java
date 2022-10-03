package money_problem.domain;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static money_problem.domain.ExchangeRate.from;
import static money_problem.domain.NewBank.withPivotCurrency;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(JUnitQuickcheck.class)
public class NewBankProperties {

    public static final String MINIMUM_RATE = "0.000001";
    public static final String MAXIMUM_RATE = "100000";

    @Property
    public void canNotAddAnExchangeRateForThePivotCurrencyOfTheBank(
            Currency pivotCurrency,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        assertThat(withPivotCurrency(pivotCurrency)
                .add(createExchangeRate(pivotCurrency, validRate)))
                .containsOnLeft(new Error("Can not add an exchange rate for the pivot currency"));
    }

    @Property
    public void canAddAnExchangeRateForAnyCurrencyDifferentFromThePivot(
            Currency pivotCurrency,
            Currency otherCurrency,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        assumeTrue(pivotCurrency != otherCurrency);

        assertThat(withPivotCurrency(pivotCurrency)
                .add(createExchangeRate(otherCurrency, validRate)))
                .isRight();
    }

    private ExchangeRate createExchangeRate(Currency pivotCurrency, double validAmount) {
        return from(validAmount, pivotCurrency).get();
    }
}