package abm.elfarolbar.simulations.reporting;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class PatronSetupDetailsReport {
    @NonNull
    String decisionStrategyName;
    int count;
}
