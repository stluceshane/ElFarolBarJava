package abm.elfarolbar.simulations;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.Patron;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class Simulation {
    private final String simulationId;
    private final int barCapacity;
    @NonNull
    private final List<Integer> barPreviousHistory;
    private final int simulationLength;
    @NonNull
    private List<AgentPropsAllocation> initialAgentPropsAllocations;

    @NonNull
    private final Bar bar;
    @NonNull
    private final List<Patron> patrons;
    @NonNull
    private final List<DecisionStrategy> decisionStrategies;
    @NonNull
    private final List<ReplacementStrategy> replacementStrategies;
    @NonNull
    private final List<Map<String, Integer>> strategyDistributions;
}
