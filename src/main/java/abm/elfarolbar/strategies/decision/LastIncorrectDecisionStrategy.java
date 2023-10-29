package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import com.google.common.collect.Iterables;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class LastIncorrectDecisionStrategy extends CrowdednessDecisionStrategy {
    public boolean decideByCrowdedness(final Bar bar, final List<Boolean> recentCrowdedHistory) {
        return recentCrowdedHistory.isEmpty() ? RandomUtils.nextBoolean() : Iterables.getLast(recentCrowdedHistory);
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
