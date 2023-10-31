package abm.elfarolbar.experiments;

import abm.elfarolbar.simulations.PatronSetupDetails;
import abm.elfarolbar.simulations.Simulation;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.decision.LastCorrectDecisionStrategy;
import abm.elfarolbar.strategies.decision.LastIncorrectDecisionStrategy;
import abm.elfarolbar.strategies.decision.NeverDecisionStrategy;
import abm.elfarolbar.strategies.replacement.FlatToleranceReplacementStrategy;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
public class ExperimentDesignerTest {
    private static final String UUID_FORMAT_REGEX =
            "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    @ParameterizedTest
    @MethodSource
    public void build_returnsCorrectNumberOfSimulations_onExperiment_with3strategies(final int totalPatrons,
                                                                                     final int distributionSize,
                                                                                     final int expectedNumberOfSimulations) throws IOException {
        final LastCorrectDecisionStrategy lastCorrectStrategy = LastCorrectDecisionStrategy.builder().build();
        final LastIncorrectDecisionStrategy lastIncorrectStrategy = LastIncorrectDecisionStrategy.builder().build();
        final NeverDecisionStrategy neverDecisionStrategy = NeverDecisionStrategy.builder().build();

        final Set<DecisionStrategy> decisionStrategies = ImmutableSet.of(
                lastCorrectStrategy,
                lastIncorrectStrategy,
                neverDecisionStrategy
        );

        final FlatToleranceReplacementStrategy flatToleranceReplacementStrategy = FlatToleranceReplacementStrategy.builder().build();

        final ExperimentDesigner experimentDesigner = ExperimentDesigner.builder()
                .barCapacity(100)
                .distributionSize(distributionSize)
                .decisionStrategies(decisionStrategies)
                .replacementStrategy(flatToleranceReplacementStrategy)
                .initialBarAttendance(0)
                .simulationLength(50)
                .totalPatrons(totalPatrons)
                .build();

        final Experiment experiment = experimentDesigner.design();

        assertThat("Expected number of simulations were created", experiment.getSimulations(), hasSize(expectedNumberOfSimulations));
    }

    @Test
    public void build_returnsExperiment() throws IOException {
        final LastCorrectDecisionStrategy lastCorrectStrategy = LastCorrectDecisionStrategy.builder().build();
        final LastIncorrectDecisionStrategy lastIncorrectStrategy = LastIncorrectDecisionStrategy.builder().build();
        final NeverDecisionStrategy neverDecisionStrategy = NeverDecisionStrategy.builder().build();

        final Set<DecisionStrategy> decisionStrategies = ImmutableSet.of(
                lastCorrectStrategy,
                lastIncorrectStrategy,
                neverDecisionStrategy
        );

        final FlatToleranceReplacementStrategy flatToleranceReplacementStrategy = FlatToleranceReplacementStrategy.builder().build();

        final ExperimentDesigner experimentDesigner = ExperimentDesigner.builder()
                .barCapacity(60)
                .distributionSize(20)
                .decisionStrategies(decisionStrategies)
                .replacementStrategy(flatToleranceReplacementStrategy)
                .initialBarAttendance(20)
                .simulationLength(50)
                .totalPatrons(100)
                .build();

        final Experiment experiment = experimentDesigner.design();

        assertThat("Experiment ID is in UUID format", experiment.getId(), matchesPattern(UUID_FORMAT_REGEX));
        assertThat("Experiment output path is to expected destination", experiment.getOutputPath(), is(String.format("./reports/%s", experiment.getId())));

        final List<Map<String, Integer>> strategyDistributions = experiment.getSimulations()
                .stream()
                .map(Simulation::getInitialPatronSetupDetails)
                .map(patronSetupDetailsList ->
                        patronSetupDetailsList.stream()
                                .collect(Collectors.toMap(PatronSetupDetails::getDecisionStrategyName, PatronSetupDetails::getCount))
                )
                .toList();
        final List<Map<String, Integer>> expectedStrategyDistributions = List.of(
                Map.of("LastCorrect", 0, "LastIncorrect", 0, "Never", 100),
                Map.of("LastCorrect", 0, "LastIncorrect", 20, "Never", 80),
                Map.of("LastCorrect", 20, "LastIncorrect", 0, "Never", 80),
                Map.of("LastCorrect", 0, "LastIncorrect", 40, "Never", 60),
                Map.of("LastCorrect", 20, "LastIncorrect", 20, "Never", 60),
                Map.of("LastCorrect", 40, "LastIncorrect", 0, "Never", 60),
                Map.of("LastCorrect", 0, "LastIncorrect", 60, "Never", 40),
                Map.of("LastCorrect", 20, "LastIncorrect", 40, "Never", 40),
                Map.of("LastCorrect", 40, "LastIncorrect", 20, "Never", 40),
                Map.of("LastCorrect", 60, "LastIncorrect", 0, "Never", 40),
                Map.of("LastCorrect", 0, "LastIncorrect", 80, "Never", 20),
                Map.of("LastCorrect", 20, "LastIncorrect", 60, "Never", 20),
                Map.of("LastCorrect", 40, "LastIncorrect", 40, "Never", 20),
                Map.of("LastCorrect", 60, "LastIncorrect", 20, "Never", 20),
                Map.of("LastCorrect", 80, "LastIncorrect", 0, "Never", 20),
                Map.of("LastCorrect", 0, "LastIncorrect", 100, "Never", 0),
                Map.of("LastCorrect", 20, "LastIncorrect", 80, "Never", 0),
                Map.of("LastCorrect", 40, "LastIncorrect", 60, "Never", 0),
                Map.of("LastCorrect", 60, "LastIncorrect", 40, "Never", 0),
                Map.of("LastCorrect", 80, "LastIncorrect", 20, "Never", 0),
                Map.of("LastCorrect", 100, "LastIncorrect", 0, "Never", 0)
        );
        assertThat("Expected number of strategy distributions", strategyDistributions, hasSize(expectedStrategyDistributions.size()));
        expectedStrategyDistributions.forEach(expectedStrategyDistribution ->
                assertThat("Expected list of strategy distributions", strategyDistributions, hasItem(expectedStrategyDistribution))
        );

        experiment.getSimulations()
                .forEach(simulation -> {
                    assertAll("Simulation configured with correct bar",
                            () -> assertThat("Simulation configured with correct bar capacity", simulation.getBar().getMaxCapacity(), is(60)),
                            () -> assertThat("Simulation configured with correct attendance history", simulation.getBar().getAttendanceHistory(), is(List.of(20))),
                            () -> assertThat("Simulation configured with correct total population", simulation.getBar().getTotalPopulation(), is(100)),
                            () -> assertThat("Simulation configured with correct attendance", simulation.getBar().getAttendance(), is(0))
                    );
                    assertThat("Simulation configured with correct simulation length", simulation.getSimulationLength(), is(50));
                    assertThat("Simulation configured with correct bar history", simulation.getBarPreviousHistory(), is(List.of(20)));
                    assertThat("Simulation configured with correct decision strategies", simulation.getDecisionStrategies(), is(decisionStrategies));
                    assertThat("Simulation configured with correct replacement strategies", simulation.getReplacementStrategies(), is(Set.of(flatToleranceReplacementStrategy)));
                });
    }

    private static Stream<Arguments> build_returnsCorrectNumberOfSimulations_onExperiment_with3strategies() {
        return Stream.of(
                Arguments.of(100, 2, 1326),
                Arguments.of(100, 5, 231),
                Arguments.of(100, 10, 66),
                Arguments.of(100, 20, 21)
        );
    }
}
