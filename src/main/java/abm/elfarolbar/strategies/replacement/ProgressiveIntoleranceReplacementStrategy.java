package abm.elfarolbar.strategies.replacement;

import abm.elfarolbar.agents.patron.PatronHistoryEvent;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import java.util.List;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder
public class ProgressiveIntoleranceReplacementStrategy extends ReplacementStrategy {
    public boolean decidePostCheck(final PatronMemoryProps props, final List<PatronHistoryEvent> historyEvent) {
        return RandomUtils.nextDouble(0.0, 1.0) < (props.getFailureTolerance() * this.getFailedEvents(historyEvent).size());
    }
}
