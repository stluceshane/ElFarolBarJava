package abm.elfarolbar.exceptions;

public class SimulationReportingFailureException extends RuntimeException {
    public SimulationReportingFailureException(final String message, final Exception ex) {
        super(message, ex);
    }
}
