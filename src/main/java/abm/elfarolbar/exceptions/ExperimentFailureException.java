package abm.elfarolbar.exceptions;

public class ExperimentFailureException extends RuntimeException {
    public ExperimentFailureException(final String message, final Exception ex) {
        super(message, ex);
    }
}
