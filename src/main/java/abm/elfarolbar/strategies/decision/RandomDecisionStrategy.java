package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import lombok.Builder;
import org.apache.commons.lang3.RandomUtils;

@Builder(toBuilder = true)
public class RandomDecisionStrategy extends DecisionStrategy {
    @Override
    public boolean decide(final Bar bar) {
        return RandomUtils.nextInt(0, bar.getTotalPopulation() + 1) <= bar.getMaxCapacity();
    }

    @Override
    public String getName() {
        return "Random";
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
