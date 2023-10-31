package abm.elfarolbar.experiments;

import abm.elfarolbar.simulations.PatronSetupDetails;
import abm.elfarolbar.simulations.Simulation;
import abm.elfarolbar.simulations.SimulationGenerator;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import com.google.common.collect.Streams;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Builder
@Log4j2
public class ExperimentDesigner {
    private final Set<DecisionStrategy> decisionStrategies;
    private final ReplacementStrategy replacementStrategy;
    private final int distributionSize;
    private final int totalPatrons;
    private final int barCapacity;
    private final int simulationLength;
    @Builder.Default
    private final Integer initialBarAttendance = 0;

    public Experiment design() throws IOException {
        final String executionRunId = UUID.randomUUID().toString();
        final String outputPath = String.format("./reports/%s", executionRunId);
        Files.createDirectories(Paths.get(outputPath));

        final Stream<List<Integer>> distributionsStream = generateInitialDistributions().parallelStream();
        final List<Simulation> simulations = distributionsStream.map(this::generateSimulation).toList();

        return Experiment.builder()
                .id(executionRunId)
                .outputPath(outputPath)
                .simulations(simulations)
                .build();
    }

    private Simulation generateSimulation(final List<Integer> distribution) {
        final List<PatronSetupDetails> patronSetupDetailsList = getPatronSetupDetailsList(distribution);

        final String simulationExecutionId = patronSetupDetailsList.stream()
                .map(patronSetupDetails -> String.format("%s=%d", patronSetupDetails.getDecisionStrategyName(), patronSetupDetails.getCount()))
                .sorted()
                .collect(Collectors.joining("-"));

        final List<Integer> barHistory = Stream.of(initialBarAttendance)
                .filter(Objects::nonNull)
                .toList();

        final SimulationGenerator simulationGenerator = SimulationGenerator.builder()
                .barCapacity(barCapacity)
                .simulationLength(simulationLength)
                .barPreviousHistory(barHistory)
                .patronSetupDetailsList(patronSetupDetailsList)
                .decisionStrategies(decisionStrategies)
                .replacementStrategies(Set.of(replacementStrategy))
                .build();

        return simulationGenerator.generate(simulationExecutionId);
    }

    private List<PatronSetupDetails> getPatronSetupDetailsList(final List<Integer> distribution) {
        return Streams.zip(distribution.stream(), decisionStrategies.stream(), Pair::of)
                .map(pair -> PatronSetupDetails.builder()
                        .decisionStrategyName(pair.getRight().getName())
                        .replacementStrategyName(replacementStrategy.getName())
                        .count(pair.getLeft())
                        .build())
                .collect(Collectors.toList());
    }

    private List<List<Integer>> generateInitialDistributions() {
        final int dimensions = decisionStrategies.size();
        final int numLinearPoints = totalPatrons / distributionSize + 1;
        final List<Integer> linearlySpacedPoints = linearSpacing(0, totalPatrons, numLinearPoints);

        final Stream<List<Integer>> linearlySpacedPointsStream = linearlySpacedPoints.parallelStream()
                .flatMap(singlePoint -> generateDistributions(List.of(singlePoint), linearlySpacedPoints, dimensions - 1));

        return linearlySpacedPointsStream
                .map(distribution -> {
                    final int num = totalPatrons - calculateSum(distribution);
                    return Stream.concat(distribution.stream(), Stream.of(num)).collect(Collectors.toList());
                })
                .collect(Collectors.toList());
    }

    private Stream<List<Integer>> generateDistributions(final List<Integer> distribution,
                                                        final List<Integer> linearlySpacedPoints,
                                                        final int dimensions) {
        if(distribution.size() >= dimensions) {
            return Stream.of(distribution);
        }

        return linearlySpacedPoints.stream()
                .filter(num -> num + calculateSum(distribution) <= totalPatrons)
                .map(num -> Stream.concat(distribution.stream(), Stream.of(num)).collect(Collectors.toList()))
                .flatMap(distributionList -> generateDistributions(distributionList, linearlySpacedPoints, dimensions));
    }

    private int calculateSum(List<Integer> distribution) {
        return distribution.stream().mapToInt(Integer::intValue).sum();
    }

    private List<Integer> linearSpacing(final int start, final int stop, final int numPoints) {
        final int range = stop - start;
        return IntStream.range(0, numPoints)
                .map(idx -> start + (range * idx / (numPoints - 1)))
                .boxed()
                .distinct()
                .collect(Collectors.toList());
    }
}
