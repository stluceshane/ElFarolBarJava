package abm.elfarolbar.strategies.replacement;

import abm.elfarolbar.agents.patron.PatronHistoryEvent;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomUtils;

@Builder
@EqualsAndHashCode(callSuper = true)
public class FlatToleranceReplacementStrategy extends ReplacementStrategy {
    protected boolean decidePostCheck(final PatronMemoryProps props, final List<PatronHistoryEvent> historyEvent) {
        return RandomUtils.nextDouble(0.0, 1.0) < props.getFailureTolerance();
    }

    @Override
    public String getName() {
        return "FlatTolerance";
    }
}
