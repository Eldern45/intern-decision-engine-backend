# InBank TICKET-101 Validation

## Overall Observation

For the most part, the backend and frontend code work correctly. The intern used service and controller layers, where the service is responsible for the main logic and the controller manages HTTP requests. The frontend is also well-structured, separating out UI components and handling API requests correctly. The code is generally clean and well-organized. It is possible to highlight where the intern has done a good job. However, there are several suggestions for improvement and one major issue that led to a violation of TICKET-101. Below is a detailed review of the intern's code:

## Done well

### Backend

- As mentioned, the intern has a distinct DecisionEngine service for handling business logic and separate controller classes (DecisionEngineController, DecisionRequest, DecisionResponse) for the REST interface.
- The intern created custom exceptions that are clearly named, making error handling readable. The verifyInputs method (in the DecisionEngine class) checks that the personal code is valid, the loan amount is in [2000; 10000], and the period is in [12; 60]. This ensures invalid input is handled cleanly before the main logic.
- The highestValidLoanAmount method and the rest of the logic in the calculateApprovedLoan method are correct, which might seem incorrect at first glance. The formula _**((credit modifier / loan amount) * loan period) / 10 >= 0.1**_ is basically the same as _**credit modifier * loan period >= loan amount**_. This matches the logic in highestValidLoanAmount.
- The calculateApprovedLoan method returns the maximum sum that the bank would approve (or throws an exception), regardless of the loan amount requested by the customer, which satisfies the ticket’s requirements.
- Annotating services with @Service and making the controller endpoints with @RestController and @RequestMapping("/loan") is straightforward and clear.

### Frontend

-  The separate NationalIdTextFormField class handles custom validation and formatting for the personal code, which makes this component reusable. Also it uses AutovalidateMode.onUserInteraction to give instant feedback.
-  The LoanForm widget correctly calls the backend through the ApiService.
-  If an error occurs or the loan is not approved, the UI shows an error message, which ver well improves usability.

## Suggested Improvements

### Backend

- There are 2 different approaches to handle exceptions in DecisionEngine class. It is recommended to choose one approach. In this realization DecisionEngineController class handles exceptions, so DecisionEngine should just throw them. For example, instead of
```
try {
    verifyInputs(personalCode, loanAmount, loanPeriod);
} catch (Exception e) {
    return new Decision(null, null, e.getMessage());
}
```
- simply call `verifyInputs(personalCode, loanAmount, loanPeriod);`
- (I am not sure.) The creditModifier is only relevant to a single calculation. Storing it as a class-level field implies that it can be used by multiple threads simultaneously because DecisionEngine is a singleton bean. The field would be overwritten whenever two or more requests are processed at once, which could cause incorrect results. It is better to declare creditModifier in the method.
- (Also not sure.) the response bean in DecisionEngineController is not defined with request scope, so the data could be corrupted between requests.
- Single Responsibility Principle in the DesicionEngine class is violated. That is not really an issue in this realization but it is better to divide this class by two (For example, LoanValidator and DecisionEngine). If the program is more complex, then the validation logic will be easier to change.

### Frontend

- The intern ignored the fixes suggested by dart analysis.

## Issues

- The MAXIMUM_LOAN_PERIOD constant was incorrectly put. The value should be 48 instead of 60.
- The frontend has the same issue. UI allows up to 60 months while should allow only 48.
- The UI showed that the minimum loan period is 6 months, although it is actually 12 months.

All the issues were fixed.

## The Most Important Shortcoming
- "The idea of the decision engine is to determine what would be the maximum sum, regardless of the person requested loan amount". While the backend actually fulfills this request, there is an issue in the frontend: specifically, in the LoanForm class. The _submitForm method reassigns the approval loan amount and period if _**tempAmount (from the backend) > _loanAmount (chosen in the UI) && tempPeriod <= _loanPeriod**_. This validation violates the purpose of the decision engine. Therefore, this behavior was fixed and now the UI displays the maximum sum, regardless of the person requested loan amount, as it should.
