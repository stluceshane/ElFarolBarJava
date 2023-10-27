package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import lombok.Builder;

@Builder(toBuilder = true)
public class NeverDecisionStrategy extends DecisionStrategy {
    @Override
    public boolean decide(final Bar bar, final PatronMemoryProps memoryProps) {
        return false;
    }

    @Override
    public String getName() {
        return "Never";
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
