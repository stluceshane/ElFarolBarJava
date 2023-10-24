package abm.elfarolbar.agents.patron;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Builder
@Data
public class PatronReport {
    @NonNull
    private String id;
    @NonNull
    private List<PatronHistoryEvent> history;
    @NonNull
    private PatronMemoryProps memoryProps;
    private int patience;
    private int lastStrategySwitchStep;
}
