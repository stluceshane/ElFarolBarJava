package abm.elfarolbar.simulations;

import abm.elfarolbar.strategies.decision.DecisionStrategy;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;

@Builder
@Value
public class PatronSetupDetails {
    @NonNull
    String decisionStrategyName;
    @NonNull
    String replacementStrategyName;
    int count;
}
