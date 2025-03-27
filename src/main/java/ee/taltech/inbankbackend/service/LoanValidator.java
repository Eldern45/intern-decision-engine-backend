package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import org.springframework.stereotype.Component;


/** A class that provides methods for validating loan requests. **/
@Component
public class LoanValidator {

    EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();
    EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();

    /*** Verify that all inputs are valid.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     */
    public void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT
                || loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (loanPeriod < DecisionEngineConstants.MINIMUM_LOAN_PERIOD
                || loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }
    }

    /*** Get the age of the customer based on their personal ID code.
     *
     * @param code Personal ID code of the customer
     * @return Age of the customer
     * @throws PersonalCodeException If the provided personal ID code is invalid
     */
    public int getAgeFromPersonalCode(String code) throws PersonalCodeException {
        return parser.getAge(code).getYears();
    }
}
