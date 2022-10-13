package money_problem.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static money_problem.domain.Currency.*;
import static money_problem.domain.DomainUtility.*;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class BankTest {
    public static final Currency PIVOT_CURRENCY = EUR;
    private final Bank bank = Bank.withPivotCurrency(PIVOT_CURRENCY);

    private static Stream<Arguments> examplesOfDirectConversion() {
        return Stream.of(
                of(dollars(87), EUR, euros(72.5)),
                of(koreanWons(1_009_765), EUR, euros(751.313244047619)),
                of(euros(10), USD, dollars(12)),
                of(euros(543.98), USD, dollars(652.776))
        );
    }

    @ParameterizedTest
    @MethodSource("examplesOfDirectConversion")
    void convertDirectly(Money money, Currency to, Money expectedResult) {
        assertConversion(money, to, expectedResult);
    }

    private static Stream<Arguments> examplesOfConversionThroughPivotCurrency() {
        return Stream.of(
                of(dollars(10), KRW, koreanWons(11200)),
                of(dollars(-1), KRW, koreanWons(-1120)),
                of(koreanWons(39_345.50), USD, dollars(35.129910714285714)),
                of(koreanWons(1000), USD, dollars(0.8928571428571427))
        );
    }

    @ParameterizedTest
    @MethodSource("examplesOfConversionThroughPivotCurrency")
    void convertThroughPivotCurrency(Money money, Currency to, Money expectedResult) {
        assertConversion(money, to, expectedResult);
    }

    private void assertConversion(Money money, Currency to, Money expectedResult) {
        assertThat(bank.add(rateFor(1.2, USD))
                .flatMap(b -> b.add(rateFor(1344, KRW)))
                .flatMap(newBank -> newBank.convert(money, to)))
                .containsOnRight(expectedResult);
    }
}
