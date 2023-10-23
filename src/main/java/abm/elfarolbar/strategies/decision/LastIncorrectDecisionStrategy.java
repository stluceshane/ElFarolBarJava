package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import java.util.List;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
public class LastIncorrectDecisionStrategy extends CrowdednessDecisionStrategy {
    public boolean decideByCrowdedness(final Bar bar, final List<Boolean> recentCrowdedHistory) {
        return recentCrowdedHistory.isEmpty() ? RandomUtils.nextBoolean() : recentCrowdedHistory.getLast();
    }

    @Override
    public String getName() {
        return "LastIncorrect";
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
