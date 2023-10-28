package abm.elfarolbar.agents.patron;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder(toBuilder = true)
@Data
public class PatronHistoryEvent {
    @NonNull
    @Builder.Default
    private final Map<String, Boolean> strategyNameToDecisionMap = ImmutableMap.of();
    @NonNull
    private final Boolean decision;
    @NonNull
    private final String strategy;
    private Boolean overcrowded;
    private Boolean correct;
}
