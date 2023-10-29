package abm.elfarolbar.simulations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertAll;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.Patron;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.AlwaysDecisionStrategy;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.decision.LastCorrectDecisionStrategy;
import abm.elfarolbar.strategies.decision.NeverDecisionStrategy;
import abm.elfarolbar.strategies.replacement.FlatToleranceReplacementStrategy;
import abm.elfarolbar.strategies.replacement.ProgressiveIntoleranceReplacementStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimulationGeneratorTest {
    private final DecisionStrategy alwaysDecisionStrategy = AlwaysDecisionStrategy.builder().build();
    private final PatronSetupDetails alwaysStrategySetupDetails =
            PatronSetupDetails.builder()
                .replacementStrategy(FlatToleranceReplacementStrategy.builder().build())
                .decisionStrategy(alwaysDecisionStrategy)
                .count(20)
                .build();
    private final DecisionStrategy alwaysDecisionStrategy2 = AlwaysDecisionStrategy.builder().build();
    private final PatronSetupDetails alwaysStrategySetupDetails2 =
            PatronSetupDetails.builder()
                .replacementStrategy(FlatToleranceReplacementStrategy.builder().build())
                .decisionStrategy(alwaysDecisionStrategy2)
                .count(20)
                .build();
    private final DecisionStrategy neverDecisionStrategy = NeverDecisionStrategy.builder().build();
    private final PatronSetupDetails neverStrategySetupDetails =
            PatronSetupDetails.builder()
                .replacementStrategy(FlatToleranceReplacementStrategy.builder().build())
                .decisionStrategy(neverDecisionStrategy)
                .count(50)
                .build();
    private final DecisionStrategy lastCorrectDecisionStrategy = LastCorrectDecisionStrategy.builder().build();
    private final PatronSetupDetails lastCorrectStrategySetupDetails =
            PatronSetupDetails.builder()
                .replacementStrategy(ProgressiveIntoleranceReplacementStrategy.builder().build())
                .decisionStrategy(lastCorrectDecisionStrategy)
                .count(60)
                .build();

    private final SimulationGenerator testSimulationGenerator = SimulationGenerator.builder()
        .barCapacity(50)
        .simulationLength(200)
        .barPreviousHistory(List.of(10))
        .patronSetupDetailsList(ImmutableList.of(
            alwaysStrategySetupDetails,
            alwaysStrategySetupDetails2,
            neverStrategySetupDetails,
            lastCorrectStrategySetupDetails
        ))
        .build();
    @Test
    public void default_setsExpectedParameters() {
        final SimulationGenerator simulationGenerator = SimulationGenerator.builder().build();
        final SimulationGenerator expectedsimulationGenerator = SimulationGenerator.builder()
            .barCapacity(60)
            .simulationLength(100)
            .barPreviousHistory(List.of(0))
            .patronSetupDetailsList(ImmutableList.of())
            .build();

        assertThat("Default Simulation Generator built as expected", simulationGenerator, samePropertyValuesAs(expectedsimulationGenerator));
    }
    @Test
    public void generate_returnsSimulationWithCorrectMetadata() {
        final Simulation simulation = testSimulationGenerator.generate("simulation id");

        assertThat("Simulation contains correct simulation id", simulation.getSimulationId(), is("simulation id"));
        assertThat("Simulation contains correct bar capacity", simulation.getBarCapacity(), is(50));
        assertThat("Simulation contains correct bar previous history", simulation.getBarPreviousHistory(), is(ImmutableList.of(10)));
        assertThat("Simulation contains correct simulation length", simulation.getSimulationLength(), is(200));

        assertThat("Simulation contains correct patron setup details", simulation.getInitialPatronSetupDetails(), is(testSimulationGenerator.getPatronSetupDetailsList()));

        final Bar expectedBar = Bar.builder()
            .totalPopulation(150)
            .maxCapacity(50)
            .attendance(0)
            .attendanceHistory(ImmutableList.of(10))
            .build();
        assertThat("Bar is created with expected properties", simulation.getBar(), is(expectedBar));

        final Map<String, List<Patron>> decisionStrategyToPatronMap = simulation.getPatrons().stream()
                .collect(Collectors.groupingBy(Patron::getDecisionStrategyName));
        final Map<String, List<Patron>> replacementStrategyToPatronMap = simulation.getPatrons().stream()
                .collect(Collectors.groupingBy(patron -> patron.getReplacementStrategy().getClass().getName()));
        assertAll("Simulation contains correctly setup patrons",
            () -> assertThat("Simulation contains correct number of patrons", simulation.getPatrons().size(), is(150)),
            () -> assertThat("Simulation contains correct number of patrons with Always decision strategy",
                decisionStrategyToPatronMap.get(alwaysDecisionStrategy.getName()).size(), is(40)),
            () -> assertThat("Simulation contains correct number of patrons with Never decision strategy",
                decisionStrategyToPatronMap.get(neverDecisionStrategy.getName()).size(), is(50)),
            () -> assertThat("Simulation contains correct number of patrons with Last Correct decision strategy",
                decisionStrategyToPatronMap.get(lastCorrectDecisionStrategy.getName()).size(), is(60)),
            () -> assertThat("Simulation contains correct number of patrons with Flat Tolerance replacement strategy",
                replacementStrategyToPatronMap.get(FlatToleranceReplacementStrategy.class.getName()).size(), is(90)),
            () -> assertThat("Simulation contains correct number of patrons with Progressive Intolerance replacement strategy",
                replacementStrategyToPatronMap.get(ProgressiveIntoleranceReplacementStrategy.class.getName()).size(), is(60))
        );

        final Map<String, DecisionStrategy> expectedDecisionStrategiesMap = ImmutableMap.of(
            alwaysDecisionStrategy.getName(), alwaysDecisionStrategy,
            neverDecisionStrategy.getName(), neverDecisionStrategy,
            lastCorrectDecisionStrategy.getName(), lastCorrectDecisionStrategy
        );
        final PatronMemoryProps expectedPatronMemoryProps = PatronMemoryProps.builder()
            .failureTolerance(0.1f)
            .memoryLength(5)
            .build();
        simulation.getPatrons()
            .forEach(patron -> {
                assertThat("Patron contains correct decision strategies map", patron.getDecisionStrategies(), is(expectedDecisionStrategiesMap));
                assertThat("Patron contains correct memory props", patron.getMemoryProps(), is(expectedPatronMemoryProps));
            });

        assertThat("Simulation contains all used decision strategies", simulation.getDecisionStrategies(), is(
            ImmutableSet.of(alwaysDecisionStrategy, neverDecisionStrategy, lastCorrectDecisionStrategy))
        );
        assertAll("Simulation contains all used replacement strategies",
            () -> assertThat("Replacement strategies contains two items", simulation.getReplacementStrategies(), hasSize(2)),
            () -> assertThat("Replacement strategies contains Flat Tolerance strategy", simulation.getReplacementStrategies(),
                hasItem(samePropertyValuesAs(FlatToleranceReplacementStrategy.builder().build()))
            ),
            () -> assertThat("Replacement strategies contains Flat Tolerance strategy", simulation.getReplacementStrategies(),
                hasItem(samePropertyValuesAs(ProgressiveIntoleranceReplacementStrategy.builder().build()))
            )
        );
        assertThat("Strategy distributions list is empty", simulation.getStrategyDistributions().isEmpty());
    }
}
