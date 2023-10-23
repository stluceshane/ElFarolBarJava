package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
public class PureRandomDecisionStrategy extends DecisionStrategy {
    @Override
    public boolean decide(final Bar bar) {
        return RandomUtils.nextBoolean();
    }

    @Override
    public String getName() {
        return "PureRandom";
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
