package money_problem.domain.properties;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import money_problem.domain.Bank;
import money_problem.domain.Currency;
import money_problem.domain.Money;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vavr.collection.HashMap.of;
import static money_problem.domain.Currency.*;
import static money_problem.domain.DomainUtility.euros;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class BankProperties {
    private final Bank bank;

    private final Map<Tuple2<Currency, Currency>, Double> exchangeRates =
            of(
                    Tuple.of(EUR, USD), 1.0567,
                    Tuple.of(USD, EUR), 0.9466,
                    Tuple.of(USD, KRW), 1302.0811,
                    Tuple.of(KRW, USD), 0.00076801737,
                    Tuple.of(EUR, KRW), 1368.51779,
                    Tuple.of(KRW, EUR), 0.00073
            );

    public BankProperties() {
        this.bank = createBank();
    }

    @Property
    public void convertInSameCurrencyShouldReturnOriginalMoney(@From(MoneyGenerator.class) Money originalMoney) {
        assertThat(bank.convert(originalMoney, originalMoney.currency()))
                .containsOnRight(originalMoney);
    }

    @Property
    public void roundTrippingInDifferentCurrencies(@From(MoneyGenerator.class) Money originalMoney,
                                                   Currency to) {
        assertThat(roundTripConvert(originalMoney, to))
                .hasRightValueSatisfying(roundTripMoney ->
                        assertThatAmountAreClosed(originalMoney, roundTripMoney)
                );
    }

    @Test
    public void roundTripInError() {
        var originalMoney = euros(-3.3492930734190595E8);
        assertThat(roundTripConvert(originalMoney, KRW))
                .hasRightValueSatisfying(money -> assertThatAmountAreClosed(originalMoney, money));
    }

    private Either<String, Money> roundTripConvert(Money originalMoney, Currency to) {
        return bank.convert(originalMoney, to)
                .flatMap(convertedMoney -> bank.convert(convertedMoney, originalMoney.currency()));
    }

    private void assertThatAmountAreClosed(Money originalMoney, Money roundTripMoney) {
        Assertions.assertThat(
                Math.abs(roundTripMoney.amount() - originalMoney.amount()) < toleranceFor(originalMoney)
        ).isTrue();
    }

    private double toleranceFor(Money originalMoney) {
        return Math.abs(0.001 * originalMoney.amount());
    }

    private Bank createBank() {
        return exchangeRates
                .foldLeft(newBank(), (bank, exchangeRate) -> bank.addExchangeRate(exchangeRate._1._1, exchangeRate._1._2, exchangeRate._2));
    }

    private Bank newBank() {
        var firstEntry = exchangeRates.head();
        return Bank.withExchangeRate(firstEntry._1()._1, firstEntry._1()._2, firstEntry._2);
    }
}
