package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * (which is determined by the last four digits of their ID code) and age factor.
 */
@Service
public class DecisionEngine {

    // Validator for loan requests
    private final LoanValidator validator;

    public DecisionEngine(LoanValidator loanValidator) {
        this.validator = loanValidator;
    }


    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     * @throws AgeRestrictionException If the customer is too young or too old for a loan
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, AgeRestrictionException, PersonalCodeException {

        validator.verifyInputs(personalCode, loanAmount, loanPeriod);
        int creditModifier = getCreditModifier(personalCode);

        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        while (highestValidLoanAmount(personalCode, creditModifier, loanPeriod) < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
            loanPeriod++;
            // The business rules do not specify a loan step size, so we assume it is 1 month. It could be 6 months,
            // (like on InBank's website) but the task description does not specify this. So I commented out the line below.
            //loanPeriod+=6;
        }

        int outputLoanAmount;
        if (loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            outputLoanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, highestValidLoanAmount(personalCode, creditModifier, loanPeriod));
        } else {
            throw new NoValidLoanException("No valid loan found!");
        }

        return new Decision(outputLoanAmount, loanPeriod, null);
    }


    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     * The loan amount is calculated based on the customer's credit modifier, age factor and loan period.
     * If age is below 30, the age factor increases from 0.5 to 1.0 linearly.
     * If age is between 30 and 50, the age factor is 1.0.
     * If age is above 50, the age factor decreases from 1.0 to 0.5 linearly.
     * @return Largest valid loan amount
     */
    private int highestValidLoanAmount(String personalCode, int creditModifier, int loanPeriod) throws PersonalCodeException, AgeRestrictionException {
        int age = validator.getAgeFromPersonalCode(personalCode);
        double ageFactor;

        if (age <= DecisionEngineConstants.MINIMUM_AGE) {
            throw new AgeRestrictionException("Customer is too young for a loan.");
        } else if (age < 30) {
            // Factor increases from 0.5 to 1.0 linearly
            ageFactor = 0.5 + (age - 19) * (0.5 / (30 - 19));
        } else if (age <= 50) {
            ageFactor = 1.0;
        } else if (age < DecisionEngineConstants.MAXIMUM_AGE) {
            // Factor decreases from 1.0 to 0.5 linearly
            ageFactor = 1.0 - (age - 50) * (0.5 / (70 - 50));
        } else {
            throw new AgeRestrictionException("Customer is too old for a loan.");
        }

        return (int) (creditModifier * loanPeriod * ageFactor);
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }
}
