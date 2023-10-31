package abm.elfarolbar.experiments;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class ExperimentResults {
    @NonNull
    Experiment experiment;
    long executionTime;
}
