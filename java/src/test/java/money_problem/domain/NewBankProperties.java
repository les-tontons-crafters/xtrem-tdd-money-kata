package money_problem.domain;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class NewBankProperties {
    @Property
    public void canNotAddAnExchangeRateForThePivotCurrencyOfTheBank(Currency pivotCurrency, @InRange(min = "0.000001", max = "100000") double validAmount) {
        assertThat(NewBank.withPivotCurrency(pivotCurrency)
                .add(new ExchangeRate(validAmount, pivotCurrency)))
                .containsOnLeft(new Error("Can not add an exchange rate for the pivot currency"));
    }
}