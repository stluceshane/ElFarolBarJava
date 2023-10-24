package abm.elfarolbar.strategies.replacement;

import abm.elfarolbar.agents.patron.PatronHistoryEvent;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import java.util.List;

public abstract class ReplacementStrategy {
    protected abstract boolean decidePostCheck(final PatronMemoryProps props, final List<PatronHistoryEvent> historyEvent);

    public boolean decide(final PatronMemoryProps props, final List<PatronHistoryEvent> history) {
        return !history.isEmpty()
            && !history.getLast().getCorrect()
            && this.minRequirement(history)
            && this.decidePostCheck(props, history);
    }

    protected boolean minRequirement(final List<PatronHistoryEvent> history) {
        return this.getFailedEvents(history).size() * 2 >= history.size();
    }

    protected List<PatronHistoryEvent> getFailedEvents(final List<PatronHistoryEvent> history) {
        return history.stream()
                .filter(historyEvent -> !historyEvent.getCorrect())
                .toList();
    }
}
