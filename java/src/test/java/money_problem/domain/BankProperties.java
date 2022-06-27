package money_problem.domain;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vavr.collection.HashMap.of;
import static money_problem.domain.Currency.*;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class BankProperties {
    private final Bank bank;
    private final Map<Tuple2<Currency, Currency>, Double> exchangeRates =
            of(
                    Tuple.of(EUR, USD), 1.2,
                    Tuple.of(USD, EUR), 0.82,
                    Tuple.of(USD, KRW), 1100d,
                    Tuple.of(KRW, USD), 0.0009,
                    Tuple.of(EUR, KRW), 1344d,
                    Tuple.of(KRW, EUR), 0.00073
            );

    public BankProperties() {
        this.bank = createBank();
    }

    @Property
    public void convertInSameCurrencyShouldReturnOriginalMoney(double originalAmount, Currency currency) {
        var originalMoney = new Money(originalAmount, currency);

        assertThat(bank.convert(originalMoney, currency))
                .containsOnRight(originalMoney);
    }

    @Property
    public void roundTrippingInDifferentCurrencies(double originalAmount, Currency from, Currency to) {
        assertThat(roundTripConvert(originalAmount, from, to))
                .hasRightValueSatisfying(money ->
                        assertThatAmountAreClosed(originalAmount, money)
                );
    }

    @Test
    public void roundTripInError() {
        var originalAmount = 9.051E-11;
        assertThat(roundTripConvert(originalAmount, EUR, USD))
                .hasRightValueSatisfying(money -> assertThatAmountAreClosed(originalAmount, money));
    }

    private AbstractDoubleAssert<?> assertThatAmountAreClosed(double originalAmount, Money money) {
        return Assertions.assertThat(money.amount())
                .isCloseTo(originalAmount, Offset.offset(0.1));
    }

    private Either<String, Money> roundTripConvert(double originalAmount, Currency from, Currency to) {
        return bank.convert(new Money(originalAmount, from), to)
                .flatMap(convertedMoney -> bank.convert(convertedMoney, from));
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
