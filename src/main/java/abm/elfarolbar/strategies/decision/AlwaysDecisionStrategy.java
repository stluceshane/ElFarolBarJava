package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import lombok.Builder;

@Builder(toBuilder = true)
public class AlwaysDecisionStrategy extends DecisionStrategy {
    @Override
    public boolean decide(final Bar bar, final PatronMemoryProps memoryProps) {
        return true;
    }

    @Override
    public String getName() {
        return "Always";
    }

    @Override
    public DecisionStrategy clone() {
        return this.toBuilder().build();
    }
}
