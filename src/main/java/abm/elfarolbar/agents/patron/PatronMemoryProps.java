package abm.elfarolbar.agents.patron;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PatronMemoryProps {
    @Builder.Default
    float failureTolerance = 0.1f;
    @Builder.Default
    int memoryLength = 5;
}
