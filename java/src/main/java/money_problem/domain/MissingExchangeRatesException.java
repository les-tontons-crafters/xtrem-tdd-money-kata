package money_problem.domain;

import java.util.List;
import java.util.stream.Collectors;

public class MissingExchangeRatesException extends Exception {
    public MissingExchangeRatesException(List<MissingExchangeRateException> missingExchangeRates) {
        super(missingExchangeRates.stream()
                .map(e -> String.format("[%s]", e.getMessage()))
                .collect(Collectors.joining(",", "Missing exchange rate(s): ", "")));
    }
}
