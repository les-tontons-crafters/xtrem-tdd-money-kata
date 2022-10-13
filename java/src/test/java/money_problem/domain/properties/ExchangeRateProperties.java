package money_problem.domain.properties;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import money_problem.domain.Currency;
import money_problem.domain.Error;
import money_problem.domain.ExchangeRate;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.runner.RunWith;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class ExchangeRateProperties {
    @Property
    public void canNotUseANegativeDoubleOrZeroAsExchangeRate(
            @InRange(max = "0") double invalidAmount,
            Currency anyCurrency) {
        VavrAssertions.assertThat(ExchangeRate.from(invalidAmount, anyCurrency))
                .containsOnLeft(new Error("Exchange rate should be greater than 0"));
    }
}