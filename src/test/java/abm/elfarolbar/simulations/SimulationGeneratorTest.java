package abm.elfarolbar.simulations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertAll;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.strategies.decision.AlwaysDecisionStrategy;
import abm.elfarolbar.strategies.decision.LastCorrectDecisionStrategy;
import abm.elfarolbar.strategies.decision.NeverDecisionStrategy;
import abm.elfarolbar.strategies.replacement.FlatToleranceReplacementStrategy;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimulationGeneratorTest {
    private static final String UUID_FORMAT_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private final SimulationGenerator testSimulationGenerator = SimulationGenerator.builder()
        .barCapacity(50)
        .simulationLength(200)
        .barPreviousHistory(List.of(10))
        .patronSetupDetailsList(ImmutableList.of(
            PatronSetupDetails.builder()
                .replacementStrategy(FlatToleranceReplacementStrategy.builder().build())
                .decisionStrategy(AlwaysDecisionStrategy.builder().build())
                .count(50)
                .build(),
            PatronSetupDetails.builder()
                .replacementStrategy(FlatToleranceReplacementStrategy.builder().build())
                .decisionStrategy(NeverDecisionStrategy.builder().build())
                .count(50)
                .build(),
            PatronSetupDetails.builder()
                .replacementStrategy(FlatToleranceReplacementStrategy.builder().build())
                .decisionStrategy(LastCorrectDecisionStrategy.builder().build())
                .count(50)
                .build()
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
        assertAll("Simulation contains correct patrons",
            () -> assertThat("Simulation contains correct number of patrons", simulation.getPatrons().size(), is(150))
        );

        final Bar expectedBar = Bar.builder()
            .totalPopulation(150)
            .maxCapacity(50)
            .attendance(0)
            .attendanceHistory(ImmutableList.of(10))
            .build();
        assertThat("Bar is created with expected properties", simulation.getBar(), is(expectedBar));
    }
}
