package abm.elfarolbar.agents.patron;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;

@Builder(toBuilder = true)
@Data
public class PatronHistoryEvent {
    @NonNull
    @Builder.Default
    private final Map<String, Boolean> decisions = ImmutableMap.of();
    @NonNull
    private final Boolean decision;
    @NonNull
    private final String strategy;
    private Boolean crowded;
    private Boolean correct;
}
