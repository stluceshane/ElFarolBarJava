package abm.elfarolbar.strategies.decision;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import lombok.Getter;

@Getter
public abstract class DecisionStrategy {
    private int users = 0;

    public abstract boolean decide(final Bar bar, final PatronMemoryProps memoryProps);

    public abstract String getName();

    public abstract DecisionStrategy clone();

    public void reset() {
        this.users = 0;
    }

    public void select() {
        this.users++;
    }
}
