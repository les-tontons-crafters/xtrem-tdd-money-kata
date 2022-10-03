package money_problem.domain;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import money_problem.domain.properties.MoneyGenerator;
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
        notPivotCurrency(pivotCurrency, otherCurrency);

        assertThat(withPivotCurrency(pivotCurrency)
                .add(createExchangeRate(otherCurrency, validRate)))
                .isRight();
    }

    @Property
    public void canUpdateAnExchangeRateForAnyCurrencyDifferentFromThePivot(
            Currency pivotCurrency,
            Currency otherCurrency,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, otherCurrency);

        var exchangeRate = createExchangeRate(otherCurrency, validRate);
        var updatedExchangeRate = createExchangeRate(otherCurrency, validRate + 0.1);

        assertThat(withPivotCurrency(pivotCurrency)
                .add(exchangeRate)
                .flatMap(newBank -> newBank.add(updatedExchangeRate)))
                .isRight();
    }

    @Property
    public void canNotConvertToAnUnknownCurrency(
            Currency pivotCurrency,
            Currency otherCurrency,
            @From(MoneyGenerator.class) Money money) {
        assumeTrue(money.currency() != otherCurrency);

        assertThat(withPivotCurrency(pivotCurrency)
                .convert(money, otherCurrency))
                .containsOnLeft(new Error("No exchange rate defined for " + money.currency() + "->" + otherCurrency));
    }

    @Property
    public void convertAnyMoneyInPivotCurrencyToPivotCurrencyReturnMoneyItself(
            @From(MoneyGenerator.class) Money money) {
        assertThat(withPivotCurrency(money.currency())
                .convert(money, money.currency()))
                .containsOnRight(money);
    }


    private ExchangeRate createExchangeRate(Currency pivotCurrency, double validAmount) {
        return from(validAmount, pivotCurrency).get();
    }

    private void notPivotCurrency(Currency pivotCurrency, Currency otherCurrency) {
        assumeTrue(pivotCurrency != otherCurrency);
    }
}