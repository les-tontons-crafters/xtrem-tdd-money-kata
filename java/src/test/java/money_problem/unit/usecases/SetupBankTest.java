package money_problem.unit.usecases;

import money_problem.domain.Bank;
import money_problem.usecases.ports.BankRepository;
import money_problem.usecases.setup_bank.SetupBankCommand;
import money_problem.usecases.setup_bank.SetupBankUseCase;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.EUR;
import static money_problem.usecases.common.Unit.unit;
import static money_problem.usecases.common.UseCaseError.error;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.*;

class SetupBankTest {
    private final BankRepository bankRepositoryMock = mock(BankRepository.class);
    private final SetupBankUseCase setupBankUseCase = new SetupBankUseCase(bankRepositoryMock);
    private final SetupBankCommand setupBankCommand = new SetupBankCommand(EUR);

    @Test
    void return_an_error_when_bank_already_setup() {
        bankAlreadySetup();

        assertThat(setupBankUseCase.invoke(setupBankCommand))
                .containsOnLeft(error("Bank is already setup"));
    }

    @Test
    void return_a_success_when_bank_not_already_setup() {
        bankNotSetup();

        assertThat(setupBankUseCase.invoke(setupBankCommand))
                .containsOnRight(unit());

        bankHasBeenSaved();
    }

    private void bankAlreadySetup() {
        when(bankRepositoryMock.exists()).thenReturn(true);
    }

    private void bankNotSetup() {
        when(bankRepositoryMock.exists()).thenReturn(false);
    }

    private void bankHasBeenSaved() {
        verify(bankRepositoryMock, times(1))
                .save(any(Bank.class));
    }
}