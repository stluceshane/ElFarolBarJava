package abm.elfarolbar.simulations;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.Patron;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.agents.patron.PatronReport;
import abm.elfarolbar.simulations.reporting.PatronSetupDetailsReport;
import abm.elfarolbar.simulations.reporting.SimulationDataset;
import abm.elfarolbar.simulations.reporting.SimulationInputDataset;
import abm.elfarolbar.simulations.reporting.SimulationReport;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.FlatToleranceReplacementStrategy;
import abm.elfarolbar.strategies.replacement.ProgressiveIntoleranceReplacementStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import com.google.common.collect.*;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SimulationTest {
    @Mock
    private Bar bar;
    @Mock
    private Patron alwaysChangesStrategyPatron;
    @Mock
    private Patron neverChangesStrategyPatron;
    @Mock
    private Patron sometimesChangesStrategyPatron;
    @Mock
    private DecisionStrategy alwaysDecisionStrategy;
    @Mock
    private DecisionStrategy neverDecisionStrategy;
    @Mock
    private DecisionStrategy lastCorrectDecisionStrategy;
    @Mock
    private FlatToleranceReplacementStrategy flatToleranceReplacementStrategy;
    @Mock
    private ProgressiveIntoleranceReplacementStrategy progressiveIntoleranceReplacementStrategy;

    @Test
    public void execute_runsSimulation() {
        final List<Patron> patrons = ImmutableList.of(
                alwaysChangesStrategyPatron,
                neverChangesStrategyPatron,
                sometimesChangesStrategyPatron
        );

        final Set<DecisionStrategy> decisionStrategies = ImmutableSet.of(
                alwaysDecisionStrategy,
                neverDecisionStrategy,
                lastCorrectDecisionStrategy
        );

        final Set<ReplacementStrategy> replacementStrategies = ImmutableSet.of(
                flatToleranceReplacementStrategy,
                progressiveIntoleranceReplacementStrategy
        );
        doReturn(false)
                .when(neverChangesStrategyPatron)
                .shouldReplaceStrategy();
        doReturn(true)
                .when(alwaysChangesStrategyPatron)
                .shouldReplaceStrategy();

        final List<Boolean> shouldReplaceStrategyBooleanList = IntStream.range(0, 99)
                .mapToObj(idx -> RandomUtils.nextBoolean())
                .toList();
        doReturn(false, shouldReplaceStrategyBooleanList.toArray())
                .when(sometimesChangesStrategyPatron)
                .shouldReplaceStrategy();

        final Object[] alwaysDecisionStrategyUsers = IntStream.range(0, 99).map(idx -> RandomUtils.nextInt(0, 100)).boxed().toList().toArray();
        final Object[] neverDecisionStrategyUsers = IntStream.range(0, 99).map(idx -> RandomUtils.nextInt(0, 100)).boxed().toList().toArray();
        final Object[] lastCorrectDecisionStrategyUsers = IntStream.range(0, 99).map(idx -> RandomUtils.nextInt(0, 100)).boxed().toList().toArray();
        doReturn(30, alwaysDecisionStrategyUsers).when(alwaysDecisionStrategy).getUsers();
        doReturn("Always").when(alwaysDecisionStrategy).getName();
        doReturn(50, neverDecisionStrategyUsers).when(neverDecisionStrategy).getUsers();
        doReturn("Never").when(neverDecisionStrategy).getName();
        doReturn(20, lastCorrectDecisionStrategyUsers).when(lastCorrectDecisionStrategy).getUsers();
        doReturn("Last Correct").when(lastCorrectDecisionStrategy).getName();

        final Simulation simulation = Simulation.builder()
                .simulationId(UUID.randomUUID().toString())
                .barPreviousHistory(ImmutableList.of(0))
                .simulationLength(100)
                .initialPatronSetupDetails(ImmutableList.of())
                .bar(bar)
                .patrons(patrons)
                .decisionStrategies(decisionStrategies)
                .replacementStrategies(replacementStrategies)
                .strategyDistributions(Lists.newArrayList())
                .build();

        simulation.execute();

        verify(bar, times(100)).record();
        verify(bar, times(100)).reset();

        patrons.forEach(patron -> {
            verify(patron, times(100)).decide(eq(bar));
            verify(patron, times(100)).record(eq(bar));
        });

        verify(neverChangesStrategyPatron, never()).selectNewStrategy();
        verify(alwaysChangesStrategyPatron, times(100)).selectNewStrategy();

        final int numStrategyReplacementsPatron3 = Math.toIntExact(shouldReplaceStrategyBooleanList.stream()
                .filter(Boolean::booleanValue)
                .count());
        verify(sometimesChangesStrategyPatron, times(numStrategyReplacementsPatron3)).selectNewStrategy();

        decisionStrategies.forEach(decisionStrategy -> {
            verify(decisionStrategy, times(100)).getName();
            verify(decisionStrategy, times(100)).getUsers();
            verify(decisionStrategy, times(100)).reset();
        });

        assertAll("Simulation distributions are accurate",
                () -> assertThat("Correct number of strategy distributions are saved", simulation.getStrategyDistributions(), hasSize(100)),
                () -> assertThat("Always strategy users is correctly recorded", getDistributionForStrategy(simulation, "Always"), contains(prependDistribution(alwaysDecisionStrategyUsers, 30))),
                () -> assertThat("Never strategy users is correctly recorded", getDistributionForStrategy(simulation, "Never"), contains(prependDistribution(neverDecisionStrategyUsers, 50))),
                () -> assertThat("Last Correct strategy users is correctly recorded", getDistributionForStrategy(simulation, "Last Correct"), contains(prependDistribution(lastCorrectDecisionStrategyUsers, 20)))
        );
    }

    @Test
    public void generateReport_returnsDataAboutSimulation() {
        doReturn(100)
                .when(bar)
                .getMaxCapacity();
        final PatronReport alwaysChangesStrategyPatronReport = createTestPatronReport();
        doReturn(alwaysChangesStrategyPatronReport)
                .when(alwaysChangesStrategyPatron)
                .generateReport();
        final PatronReport neverChangesStrategyPatronReport = createTestPatronReport();
        doReturn(neverChangesStrategyPatronReport)
                .when(neverChangesStrategyPatron)
                .generateReport();
        final PatronReport sometimesChangesStrategyPatronReport = createTestPatronReport();
        doReturn(sometimesChangesStrategyPatronReport)
                .when(sometimesChangesStrategyPatron)
                .generateReport();
        final List<Patron> patrons = ImmutableList.of(
                alwaysChangesStrategyPatron,
                neverChangesStrategyPatron,
                sometimesChangesStrategyPatron
        );
        final List<Integer> attendanceHistory = ImmutableList.of();
        doReturn(attendanceHistory)
                .when(bar)
                .getAttendanceHistory();

        final Set<DecisionStrategy> decisionStrategies = ImmutableSet.of(
                alwaysDecisionStrategy,
                neverDecisionStrategy,
                lastCorrectDecisionStrategy
        );

        final Set<ReplacementStrategy> replacementStrategies = ImmutableSet.of(
                flatToleranceReplacementStrategy,
                progressiveIntoleranceReplacementStrategy
        );

        final List<PatronSetupDetails> patronSetupDetailsList = ImmutableList.of(
                PatronSetupDetails.builder()
                        .decisionStrategyName("Always")
                        .replacementStrategyName("FlatTolerance")
                        .count(40)
                        .build(),
                PatronSetupDetails.builder()
                        .decisionStrategyName("Never")
                        .replacementStrategyName("ProgressiveIntolerance")
                        .count(60)
                        .build()
        );

        final Simulation simulation = Simulation.builder()
                .simulationId(UUID.randomUUID().toString())
                .barPreviousHistory(ImmutableList.of(0))
                .simulationLength(75)
                .initialPatronSetupDetails(patronSetupDetailsList)
                .bar(bar)
                .patrons(patrons)
                .decisionStrategies(decisionStrategies)
                .replacementStrategies(replacementStrategies)
                .strategyDistributions(ImmutableList.of(ImmutableMap.of("Always", 35, "Never", 65)))
                .build();

        final SimulationReport report = simulation.generateReport();
        final SimulationInputDataset expectedInputDataset = SimulationInputDataset.builder()
                .barCapacity(100)
                .simulationLength(75)
                .barPreviousHistory(ImmutableList.of(0))
                .initialPatronSetupDetails(ImmutableList.of(
                        PatronSetupDetailsReport.builder()
                                .decisionStrategyName("Always")
                                .count(40)
                                .build(),
                        PatronSetupDetailsReport.builder()
                                .decisionStrategyName("Never")
                                .count(60)
                                .build()
                ))
                .build();

        assertThat("Simmulation input is reported correctly", report.getInput(), is(expectedInputDataset));

        final SimulationDataset expectedDataset = SimulationDataset.builder()
                .attendanceHistory(attendanceHistory)
                .patronReports(ImmutableList.of(
                        alwaysChangesStrategyPatronReport,
                        neverChangesStrategyPatronReport,
                        sometimesChangesStrategyPatronReport)
                )
                .strategyDistributions(simulation.getStrategyDistributions())
                .build();
        assertThat("Simmulation dataset is reported correctly", report.getDataset(), is(expectedDataset));
    }

    private static PatronReport createTestPatronReport() {
        return PatronReport.builder()
                .id(UUID.randomUUID().toString())
                .history(ImmutableList.of())
                .memoryProps(PatronMemoryProps.builder().build())
                .build();
    }

    private static List<Integer> getDistributionForStrategy(final Simulation simulation, final String strategyName) {
        return simulation.getStrategyDistributions().stream()
                .map(distribution -> distribution.get(strategyName))
                .toList();
    }

    private static Object[] prependDistribution(final Object[] distributions, final int initialDistribution) {
        return ObjectArrays.concat(initialDistribution, distributions);
    }
}
