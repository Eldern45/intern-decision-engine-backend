package ee.taltech.inbankbackend.exceptions;

public class AgeRestrictionException extends Throwable {

    private final String message;
    private final Throwable cause;

    public AgeRestrictionException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public AgeRestrictionException(String message) {
        this(message, null);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
