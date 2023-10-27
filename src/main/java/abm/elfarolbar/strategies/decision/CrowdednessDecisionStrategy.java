package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import java.util.List;

public abstract class CrowdednessDecisionStrategy extends DecisionStrategy {
    abstract boolean decideByCrowdedness(final Bar bar, final List<Boolean> recentCrowdedHistory);

    public boolean decide(final Bar bar, final PatronMemoryProps memoryProps) {
        final List<Boolean> crowdedHistory = bar.crowdedHistory();
        return decideByCrowdedness(bar, crowdedHistory.subList(Math.max(0, crowdedHistory.size() - memoryProps.getMemoryLength()), crowdedHistory.size()));
    }
}
