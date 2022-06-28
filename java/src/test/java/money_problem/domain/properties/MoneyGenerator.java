package money_problem.domain.properties;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import money_problem.domain.Currency;
import money_problem.domain.Money;

public class MoneyGenerator extends Generator<Money> {
    public static final int MAX_AMOUNT = 1_000_000_000;

    public MoneyGenerator() {
        super(Money.class);
    }

    @Override
    public Money generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        return new Money(
                sourceOfRandomness.nextDouble(-MAX_AMOUNT, MAX_AMOUNT),
                sourceOfRandomness.choose(Currency.values())
        );
    }
}
