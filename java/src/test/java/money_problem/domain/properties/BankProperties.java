package money_problem.domain.properties;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import money_problem.domain.Currency;
import money_problem.domain.Money;
import org.junit.runner.RunWith;

import static money_problem.domain.Bank.withPivotCurrency;
import static money_problem.domain.DomainUtility.*;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(JUnitQuickcheck.class)
public class BankProperties {

    @Property
    public void canNotAddAnExchangeRateForThePivotCurrencyOfTheBank(
            Currency pivotCurrency,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        assertThat(withPivotCurrency(pivotCurrency)
                .add(rateFor(validRate, pivotCurrency)))
                .containsOnLeft(error("Can not add an exchange rate for the pivot currency"));
    }

    @Property
    public void canAddAnExchangeRateForAnyCurrencyDifferentFromThePivot(
            Currency pivotCurrency,
            Currency otherCurrency,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, otherCurrency);

        assertThat(withPivotCurrency(pivotCurrency)
                .add(rateFor(validRate, otherCurrency)))
                .isRight();
    }

    @Property
    public void canUpdateAnExchangeRateForAnyCurrencyDifferentFromThePivot(
            Currency pivotCurrency,
            Currency otherCurrency,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, otherCurrency);

        var exchangeRate = rateFor(validRate, otherCurrency);
        var updatedExchangeRate = rateFor(validRate + 0.1, otherCurrency);

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
                .containsOnLeft(error(money.currency() + "->" + otherCurrency));
    }

    @Property
    public void convertAnyMoneyInPivotCurrencyToPivotCurrencyReturnMoneyItself(
            @From(MoneyGenerator.class) Money money) {
        assertThat(withPivotCurrency(money.currency())
                .convert(money, money.currency()))
                .containsOnRight(money);
    }

    @Property
    public void convertAnyMoneyToMoneyCurrencyReturnMoneyItself(
            Currency pivotCurrency,
            @From(MoneyGenerator.class) Money money,
            @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, money.currency());

        assertThat(withPivotCurrency(pivotCurrency)
                .add(rateFor(validRate, money.currency()))
                .flatMap(newBank -> newBank.convert(money, money.currency())))
                .containsOnRight(money);
    }

    private void notPivotCurrency(Currency pivotCurrency, Currency otherCurrency) {
        assumeTrue(pivotCurrency != otherCurrency);
    }
}