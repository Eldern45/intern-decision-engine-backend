# InBank Backend Service TICKET-101 Validation

## Overall Observation

For the most part, the backend code works correctly. The intern used service and controller layers, where the service is responsible for the main logic, and the controller manages HTTP requests. It is possible to highlight parts where the intern did a good job. However, there are some suggestions and one major issue, due to which the TICKET-101 was violated. Below is a detailed review of the intern's code:

## Done well

- As was already mentioned, intern has a distinct DecisionEngine service for handling business logic and separate controller classes (DecisionEngineController, DecisionRequest, DecisionResponse) for the REST interface.
- Intern did custom exceptions that are clearly named, making error handling readable. verifyInputs method (DecisionEngine class) checks that the personal code is valid, the loan amount is in [2000; 10000], and the period is in [12; 60]. This ensures invalid input is handled cleanly before the main logic.
- highestValidLoanAmount method and the rest of the logic in calculateApprovedLoan method are correct, which may seem false at first glance. The formula _**((credit modifier / loan amount) * loan period) / 10 >= 0.1**_ is basically the same as 
_**credit modifier * loan period >= loan amount**_. This matches highestValidLoanAmount method. \
- calculateApprovedLoan returns the maximum sum that the bank would approve (or throws an exception), regardless of the person requested loan amount, which satisfies the ticket description.
- Annotating services with @Service and making the controller endpoints with @RestController and @RequestMapping("/loan") is straightforward and clear.

## Suggested Improvements

- There are 2 different approaches to handle exceptions in DecisionEngine class. It is recommended to choose one approach. In this realization DecisionEngineController class handles the exceptions, so DecisionEngine should just throw them. For example, instead of
```
try {
    verifyInputs(personalCode, loanAmount, loanPeriod);
} catch (Exception e) {
    return new Decision(null, null, e.getMessage());
}
```
- just verify inputs: `verifyInputs(personalCode, loanAmount, loanPeriod);`
- The creditModifier is only relevant to a single calculation. Storing it as a class-level field implies that it can be used by multiple threads simultaneously because DecisionEngine is a singleton. The field would be overwritten whenever two or more requests are processed at once, which could cause incorrect results. It is better to declare creditModifier in the method.
