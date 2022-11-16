package money_problem.unit.domain.properties;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import money_problem.domain.Currency;
import money_problem.domain.Money;
import money_problem.unit.domain.DomainUtility;
import org.junit.runner.RunWith;

import static money_problem.domain.Bank.withPivotCurrency;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(JUnitQuickcheck.class)
public class BankProperties {

    @Property
    public void canNotAddAnExchangeRateForThePivotCurrencyOfTheBank(
            Currency pivotCurrency,
            @InRange(min = DomainUtility.MINIMUM_RATE, max = DomainUtility.MAXIMUM_RATE) double validRate) {
        assertThat(withPivotCurrency(pivotCurrency)
                .add(DomainUtility.rateFor(validRate, pivotCurrency)))
                .containsOnLeft(DomainUtility.error("Can not add an exchange rate for the pivot currency"));
    }

    @Property
    public void canAddAnExchangeRateForAnyCurrencyDifferentFromThePivot(
            Currency pivotCurrency,
            Currency otherCurrency,
            @InRange(min = DomainUtility.MINIMUM_RATE, max = DomainUtility.MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, otherCurrency);

        assertThat(withPivotCurrency(pivotCurrency)
                .add(DomainUtility.rateFor(validRate, otherCurrency)))
                .isRight();
    }

    @Property
    public void canUpdateAnExchangeRateForAnyCurrencyDifferentFromThePivot(
            Currency pivotCurrency,
            Currency otherCurrency,
            @InRange(min = DomainUtility.MINIMUM_RATE, max = DomainUtility.MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, otherCurrency);

        var exchangeRate = DomainUtility.rateFor(validRate, otherCurrency);
        var updatedExchangeRate = DomainUtility.rateFor(validRate + 0.1, otherCurrency);

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
        assumeTrue(money.getCurrency() != otherCurrency);

        assertThat(withPivotCurrency(pivotCurrency)
                .convert(money, otherCurrency))
                .containsOnLeft(DomainUtility.error(money.getCurrency() + "->" + otherCurrency));
    }

    @Property
    public void convertAnyMoneyInPivotCurrencyToPivotCurrencyReturnMoneyItself(
            @From(MoneyGenerator.class) Money money) {
        assertThat(withPivotCurrency(money.getCurrency())
                .convert(money, money.getCurrency()))
                .containsOnRight(money);
    }

    @Property
    public void convertAnyMoneyToMoneyCurrencyReturnMoneyItself(
            Currency pivotCurrency,
            @From(MoneyGenerator.class) Money money,
            @InRange(min = DomainUtility.MINIMUM_RATE, max = DomainUtility.MAXIMUM_RATE) double validRate) {
        notPivotCurrency(pivotCurrency, money.getCurrency());

        assertThat(withPivotCurrency(pivotCurrency)
                .add(DomainUtility.rateFor(validRate, money.getCurrency()))
                .flatMap(newBank -> newBank.convert(money, money.getCurrency())))
                .containsOnRight(money);
    }

    private void notPivotCurrency(Currency pivotCurrency, Currency otherCurrency) {
        assumeTrue(pivotCurrency != otherCurrency);
    }
}