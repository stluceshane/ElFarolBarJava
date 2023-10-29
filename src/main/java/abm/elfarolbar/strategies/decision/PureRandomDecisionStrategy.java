package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class PureRandomDecisionStrategy extends DecisionStrategy {
    @Override
    public boolean decide(final Bar bar, final PatronMemoryProps memoryProps) {
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
