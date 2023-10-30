package abm.elfarolbar.simulations;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.Patron;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class SimulationGenerator {
    @Builder.Default
    int barCapacity = 60;
    @Builder.Default
    int simulationLength = 100;
    @Builder.Default
    List<Integer> barPreviousHistory = ImmutableList.of(0);
    @Builder.Default
    List<PatronSetupDetails> patronSetupDetailsList = ImmutableList.of();
    @NonNull
    Set<DecisionStrategy> decisionStrategies;
    @NonNull
    Set<ReplacementStrategy> replacementStrategies;

    public Simulation generate(final String simulationId) {
        final Map<String, DecisionStrategy> decisionStrategiesMap = this.getDecisionStrategies()
                .stream()
                .collect(Collectors.toMap(DecisionStrategy::getName, strategy -> strategy, (first, second) -> first));

        final Map<String, ReplacementStrategy> replacementStrategiesMap = this.getReplacementStrategies()
                .stream()
                .collect(Collectors.toMap(ReplacementStrategy::getName, strategy -> strategy, (first, second) -> first));

        final List<Patron> patrons = this.getPatronSetupDetailsList().parallelStream()
                .map(patronSetupDetails -> IntStream.range(0, patronSetupDetails.getCount())
                        .mapToObj(i -> patronSetupDetails)
                        .collect(Collectors.toList()))
                .flatMap(Collection::parallelStream)
                .map(patronSetupDetails -> Patron.builder()
                        .id(UUID.randomUUID().toString())
                        .decisionStrategies(decisionStrategiesMap)
                        .decisionStrategyName(patronSetupDetails.getDecisionStrategyName())
                        .replacementStrategy(replacementStrategiesMap.get(patronSetupDetails.getReplacementStrategyName()))
                        .memoryProps(
                                PatronMemoryProps.builder()
                                        .failureTolerance(0.1f)
                                        .build()
                        )
                        .build())
                .toList();

        final Bar bar = Bar.builder()
            .maxCapacity(barCapacity)
            .attendanceHistory(Lists.newArrayList(barPreviousHistory))
            .totalPopulation(patrons.size())
            .build();

        return Simulation.builder()
            .simulationId(simulationId)
            .barPreviousHistory(barPreviousHistory)
            .simulationLength(simulationLength)
            .initialPatronSetupDetails(patronSetupDetailsList)
            .bar(bar)
            .patrons(patrons)
            .decisionStrategies(ImmutableSet.copyOf(decisionStrategiesMap.values()))
            .replacementStrategies(ImmutableSet.copyOf(replacementStrategiesMap.values()))
            .strategyDistributions(Lists.newArrayList())
            .build();
    }
}
