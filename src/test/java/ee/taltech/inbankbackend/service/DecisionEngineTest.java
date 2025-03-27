package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.AgeRestrictionException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionEngineTest {

    // < 19
    private static final String YOUNG_PERSONAL_CODE = "50701017921";
    // 30-50, Segment 2
    private static final String MID_PERSONAL_CODE = "49002012785";
    // 50 - 69, Segment 3
    private static final String SENIOR_PERSONAL_CODE = "37005032808";
    // >= 70, Segment 2
    private static final String TOO_OLD_PERSONAL_CODE = "35502016670";
    // Debtor
    private static final String DEBTOR_PERSONAL_CODE = "50307170158";

    private final LoanValidator loanValidator = new LoanValidator();
    private final DecisionEngine decisionEngine = new DecisionEngine(loanValidator);

    @Test
    public void testYoungCustomerThrowsAgeRestrictionException() {
        AgeRestrictionException exception = assertThrows(AgeRestrictionException.class, () -> {
            decisionEngine.calculateApprovedLoan(YOUNG_PERSONAL_CODE, 5000L, 24);
        });
        assertEquals("Customer is too young for a loan.", exception.getMessage());
    }

    @Test
    public void testTooOldCustomerThrowsAgeRestrictionException() {
        AgeRestrictionException exception = assertThrows(AgeRestrictionException.class, () -> {
            decisionEngine.calculateApprovedLoan(TOO_OLD_PERSONAL_CODE, 5000L, 36);
        });
        assertEquals("Customer is too old for a loan.", exception.getMessage());
    }

    @Test
    public void testDebtorThrowsNoValidLoanException() {
        NoValidLoanException exception = assertThrows(NoValidLoanException.class, () -> {
            decisionEngine.calculateApprovedLoan(DEBTOR_PERSONAL_CODE, 5000L, 42);
        });
        assertEquals("No valid loan found!", exception.getMessage());
    }

    @Test
    public void testMidAgeCustomerValidLoan() throws InvalidPersonalCodeException, InvalidLoanAmountException,
            InvalidLoanPeriodException, NoValidLoanException, AgeRestrictionException, PersonalCodeException {
        // Передаём корректные значения суммы и срока кредита
        Decision decision = decisionEngine.calculateApprovedLoan(MID_PERSONAL_CODE, 5000L, 24);
        assertNotNull(decision);
        // Проверяем, что возвращённый срок кредита находится в установленных границах
        assertEquals(24, (int) decision.getLoanPeriod());
        // Проверяем, что сумма кредита соответствует бизнес-правилам (от MINIMUM_LOAN_AMOUNT до MAXIMUM_LOAN_AMOUNT)
        assertEquals(2400, (int) decision.getLoanAmount());
    }

    @Test
    public void testSeniorCustomerValidLoan() throws InvalidPersonalCodeException, InvalidLoanAmountException,
            InvalidLoanPeriodException, NoValidLoanException, AgeRestrictionException, PersonalCodeException {
        Decision decision = decisionEngine.calculateApprovedLoan(SENIOR_PERSONAL_CODE, 7000L, 36);
        assertNotNull(decision);
        assertEquals(36, (int) decision.getLoanPeriod());
        assertEquals(3240, (int) decision.getLoanAmount());
    }

    @Test
    public void testInvalidLoanAmountThrowsException() {
        InvalidLoanAmountException exception = assertThrows(InvalidLoanAmountException.class, () -> {
            decisionEngine.calculateApprovedLoan(MID_PERSONAL_CODE, 1000L, 24);
        });
        assertEquals("Invalid loan amount!", exception.getMessage());
    }

    @Test
    public void testInvalidLoanPeriodThrowsException() {
        InvalidLoanPeriodException exception = assertThrows(InvalidLoanPeriodException.class, () -> {
            decisionEngine.calculateApprovedLoan(MID_PERSONAL_CODE, 5000L, 6);
        });
        assertEquals("Invalid loan period!", exception.getMessage());
    }
}
