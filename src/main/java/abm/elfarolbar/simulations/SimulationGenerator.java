package abm.elfarolbar.simulations;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.Patron;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    public Simulation generate(@NonNull final String simulationId) {
        final List<DecisionStrategy> decisionStrategies = this.getPatronSetupDetailsList().stream()
                .map(PatronSetupDetails::getDecisionStrategy)
                .distinct()
                .collect(Collectors.toList());

        final List<ReplacementStrategy> replacementStrategies = this.getPatronSetupDetailsList().stream()
                .map(PatronSetupDetails::getReplacementStrategy)
                .distinct()
                .collect(Collectors.toList());

        final Map<String, DecisionStrategy> decisionStrategiesByNameMap = decisionStrategies
                .stream()
                .collect(Collectors.toMap(DecisionStrategy::getName, strategy -> strategy));

        final List<Patron> patrons = this.getPatronSetupDetailsList().parallelStream()
                .map(patronSetupDetails -> IntStream.range(0, patronSetupDetails.getCount())
                        .mapToObj(i -> patronSetupDetails)
                        .collect(Collectors.toList()))
                .flatMap(Collection::parallelStream)
                .map(patronSetupDetails -> Patron.builder()
                        .id(UUID.randomUUID().toString())
                        .decisionStrategies(decisionStrategiesByNameMap)
                        .decisionStrategyName(patronSetupDetails.getDecisionStrategy().getName())
                        .replacementStrategy(patronSetupDetails.getReplacementStrategy())
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
            .barCapacity(barCapacity)
            .barPreviousHistory(barPreviousHistory)
            .simulationLength(simulationLength)
            .initialPatronSetupDetails(patronSetupDetailsList)
            .bar(bar)
            .patrons(patrons)
            .decisionStrategies(decisionStrategies)
            .replacementStrategies(replacementStrategies)
            .strategyDistributions(Lists.newArrayList())
            .build();
    }
}
