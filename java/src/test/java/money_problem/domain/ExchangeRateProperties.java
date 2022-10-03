package money_problem.domain;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class ExchangeRateProperties {
    @Property
    public void canNotUseANegativeDoubleOrZeroAsExchangeRate(
            @InRange(max = "0") double invalidAmount,
            Currency anyCurrency) {
        assertThat(ExchangeRate.from(invalidAmount, anyCurrency))
                .containsOnLeft(new Error("Exchange rate should be greater than 0"));
    }
}