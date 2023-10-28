package abm.elfarolbar.agents.patron;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.strategies.decision.DecisionStrategy;
import abm.elfarolbar.strategies.replacement.ReplacementStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PatronTest {
    @Mock
    private Bar bar;

    @Mock
    private ReplacementStrategy replacementStrategy;

    @Mock
    private DecisionStrategy activeDecisionStrategy;

    @Mock
    private DecisionStrategy inactiveDecisionStrategy;

    private Patron testPatron;

    @BeforeEach
    public void setup() {
        testPatron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder().build())
            .build();
    }

    @Test
    public void build_setsDefaultValuesForNonNullProperties() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder().build())
            .build();

        assertThat("Default value for patience is 5", patron.getPatience(), is(5));
        assertThat("Default value for lastStrategySwitchStep is 5", patron.getLastStrategySwitchStep(), is(0));
    }

    @Test
    public void decide_updatesHistory_andAttendsBar_whenActiveDecisionStrategyReturnsTrue() {
        doReturn(true)
            .when(activeDecisionStrategy)
            .decide(eq(bar), eq(testPatron.getMemoryProps()));
        doReturn(false)
            .when(inactiveDecisionStrategy)
            .decide(eq(bar), eq(testPatron.getMemoryProps()));

        assertThat("Returns true decision", testPatron.decide(bar));

        verify(activeDecisionStrategy).select();
        verify(inactiveDecisionStrategy, never()).select();

        final PatronHistoryEvent expectedPatronHistoryEvent = PatronHistoryEvent.builder()
            .strategyNameToDecisionMap(
                ImmutableMap.of(
                    "Active", Boolean.TRUE,
                    "Inactive", Boolean.FALSE
                )
            )
            .decision(true)
            .strategy("Active")
            .build();

        assertAll("Patron history is updated",
            () -> assertThat("Patron history contains one event", testPatron.getHistory().size(), is(1)),
            () -> assertThat("Last patron history event is updated correctly", testPatron.getHistory().get(0), is(expectedPatronHistoryEvent))
        );

        verify(bar).addPatron();
    }

    @Test
    public void decide_updatesHistory_andDoesNotAttendsBar_whenActiveDecisionStrategyReturnsFalse() {
        doReturn(false)
            .when(activeDecisionStrategy)
            .decide(eq(bar), eq(testPatron.getMemoryProps()));
        doReturn(true)
            .when(inactiveDecisionStrategy)
            .decide(eq(bar), eq(testPatron.getMemoryProps()));

        assertThat("Returns false decision", !testPatron.decide(bar));

        verify(activeDecisionStrategy).select();
        verify(inactiveDecisionStrategy, never()).select();

        final PatronHistoryEvent expectedPatronHistoryEvent = PatronHistoryEvent.builder()
            .strategyNameToDecisionMap(
                ImmutableMap.of(
                    "Active", Boolean.FALSE,
                    "Inactive", Boolean.TRUE
                )
            )
            .decision(false)
            .strategy("Active")
            .build();

        assertAll("Patron history is updated",
            () -> assertThat("Patron history contains one event", testPatron.getHistory().size(), is(1)),
            () -> assertThat("Last patron history event is updated correctly", testPatron.getHistory().get(0), is(expectedPatronHistoryEvent))
        );

        verify(bar, never()).addPatron();
    }

    @Test
    public void selectNewStrategy_picksStrategyWithTheMostCorrectDecisionsRecently_andUpdatesState() {
        final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();
        final List<PatronHistoryEvent> ignoredHistory = IntStream.range(0, 5)
            .boxed()
            .map(idx ->
                PatronHistoryEvent.builder()
                    .decision(true)
                    .strategy("Active")
                    .strategyNameToDecisionMap(
                        ImmutableMap.of(
                            "Active", true,
                            "Inactive", true,
                            "Inactive 2", false,
                            "Inactive Randomized", false
                        )
                    )
                    .overcrowded(true)
                    .build())
            .collect(Collectors.toList());
        final List<PatronHistoryEvent> recentHistory = IntStream.range(0, memoryProps.getMemoryLength())
            .boxed()
            .map(idx ->
                PatronHistoryEvent.builder()
                    .decision(true)
                    .strategy("Active")
                    .strategyNameToDecisionMap(
                        ImmutableMap.of(
                            "Active", true,
                            "Inactive", false,
                            "Inactive 2", RandomUtils.nextBoolean(),
                            "Inactive Randomized", RandomUtils.nextBoolean()
                        )
                    )
                    .overcrowded(true)
                    .build())
            .collect(Collectors.toList());
        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
            .addAll(ignoredHistory)
            .addAll(recentHistory)
            .build();

        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy,
                "Inactive 2", inactiveDecisionStrategy,
                "Inactive Randomized", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(memoryProps)
            .history(history)
            .lastStrategySwitchStep(0)
            .build();

        patron.selectNewStrategy();

        assertThat("Last strategy switch step is updated", patron.getLastStrategySwitchStep(), is(10));
        assertThat("Decision strategy is switched to correct strategy",  patron.getDecisionStrategyName(), is("Inactive"));
    }

    @Test
    public void selectNewStrategy_picksAnyNewStrategyRandomlyWithTheMostCorrectDecisionsRecently_andUpdatesState() {
        final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();
        final List<PatronHistoryEvent> ignoredHistory = IntStream.range(0, 5)
            .boxed()
            .map(idx ->
                PatronHistoryEvent.builder()
                    .decision(true)
                    .strategy("Active")
                    .strategyNameToDecisionMap(
                        ImmutableMap.of(
                            "Active", true,
                            "Inactive", true,
                            "Inactive 2", false,
                            "Inactive 3", false
                        )
                    )
                    .overcrowded(true)
                    .build())
            .collect(Collectors.toList());
        final List<PatronHistoryEvent> recentHistory = IntStream.range(0, memoryProps.getMemoryLength())
            .boxed()
            .map(idx ->
                PatronHistoryEvent.builder()
                    .decision(true)
                    .strategy("Active")
                    .strategyNameToDecisionMap(
                        ImmutableMap.of(
                            "Active", true,
                            "Inactive", false,
                            "Inactive 2", false,
                            "Inactive 3", true
                        )
                    )
                    .overcrowded(true)
                    .build())
            .collect(Collectors.toList());
        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
            .addAll(ignoredHistory)
            .addAll(recentHistory)
            .build();

        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy,
                "Inactive 2", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(memoryProps)
            .history(history)
            .lastStrategySwitchStep(0)
            .build();


        final Map<String, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> {
                patron.selectNewStrategy();
                return patron.getDecisionStrategyName();
            }));

        assertThat("Last strategy switch step is updated", patron.getLastStrategySwitchStep(), is(10));

        assertAll("New strategy is picked randomly and evenly between strategies with similar recent history",
            () -> assertThat("Returns one correctly selected new strategy half the time with correct proportions",
                Double.valueOf(mapResults.get("Inactive").size()), closeTo(500.0, 75.0)),
            () -> assertThat("Returns second correctly selected new strategy half the time with correct proportions",
                Double.valueOf(mapResults.get("Inactive 2").size()), closeTo(500.0, 75.0))
        );
    }

    @Test
    public void shouldReplaceStrategy_returnsFalse_whenHistoryIsEmpty() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder().build())
            .history(ImmutableList.of())
            .build();

        assertThat("Decisions strategy should not be replaced", !patron.shouldReplaceStrategy());
        verifyNoInteractions(replacementStrategy);
    }

    @Test
    public void shouldReplaceStrategy_returnsFalse_whenTimeSinceLastSwitch_isLessThan_patience() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder()
                .build())
            .history(ImmutableList.of(
                PatronHistoryEvent.builder()
                    .decision(true)
                    .strategy("Active")
                    .build()
            ))
            .patience(5)
            .lastStrategySwitchStep(0)
            .build();

        assertThat("Decisions strategy should not be replaced", !patron.shouldReplaceStrategy());
        verifyNoInteractions(replacementStrategy);
    }

    @Test
    public void shouldReplaceStrategy_returnsFalse_whenTimeSinceLastSwitch_isEqualTo_patience_andReplacementStrategyReturnsFalse() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder()
                .build())
            .history(IntStream.range(0, 5)
                    .boxed()
                    .map(idx ->
                        PatronHistoryEvent.builder()
                        .decision(true)
                        .strategy("Active")
                        .build())
                .collect(Collectors.toList())
            )
            .patience(5)
            .lastStrategySwitchStep(0)
            .build();

        doReturn(false)
            .when(replacementStrategy)
            .decide(eq(patron.getMemoryProps()), anyList());

        assertThat("Decisions strategy should not be replaced", !patron.shouldReplaceStrategy());
        verify(replacementStrategy).decide(eq(patron.getMemoryProps()), anyList());;
    }

    @Test
    public void shouldReplaceStrategy_returnsFalse_whenTimeSinceLastSwitch_isGreaterThan_patience_andReplacementStrategyReturnsFalse() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder()
                .build())
            .history(IntStream.range(0, 10)
                    .boxed()
                    .map(idx ->
                        PatronHistoryEvent.builder()
                        .decision(true)
                        .strategy("Active")
                        .build())
                .collect(Collectors.toList())
            )
            .patience(5)
            .lastStrategySwitchStep(0)
            .build();

        doReturn(false)
            .when(replacementStrategy)
            .decide(eq(patron.getMemoryProps()), anyList());

        assertThat("Decisions strategy should not be replaced", !patron.shouldReplaceStrategy());
        verify(replacementStrategy).decide(eq(patron.getMemoryProps()), anyList());;
    }

    @Test
    public void shouldReplaceStrategy_returnsTrue_whenTimeSinceLastSwitch_isEqualTo_patience_andReplacementStrategyReturnsTrue() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder()
                .build())
            .history(IntStream.range(0, 5)
                    .boxed()
                    .map(idx ->
                        PatronHistoryEvent.builder()
                        .decision(true)
                        .strategy("Active")
                        .build())
                .collect(Collectors.toList())
            )
            .patience(5)
            .lastStrategySwitchStep(0)
            .build();

        doReturn(true)
            .when(replacementStrategy)
            .decide(eq(patron.getMemoryProps()), anyList());

        assertThat("Decisions strategy should be replaced", patron.shouldReplaceStrategy());
        verify(replacementStrategy).decide(eq(patron.getMemoryProps()), anyList());;
    }

    @Test
    public void shouldReplaceStrategy_returnsTrue_whenTimeSinceLastSwitch_isGreaterThan_patience_andReplacementStrategyReturnsTrue() {
        final Patron patron = Patron.builder()
            .id(UUID.randomUUID().toString())
            .decisionStrategies(ImmutableMap.of(
                "Active", activeDecisionStrategy,
                "Inactive", inactiveDecisionStrategy
            ))
            .decisionStrategyName("Active")
            .replacementStrategy(replacementStrategy)
            .memoryProps(PatronMemoryProps.builder()
                .build())
            .history(IntStream.range(0, 10)
                    .boxed()
                    .map(idx ->
                        PatronHistoryEvent.builder()
                        .decision(true)
                        .strategy("Active")
                        .build())
                .collect(Collectors.toList())
            )
            .patience(5)
            .lastStrategySwitchStep(0)
            .build();

        doReturn(true)
            .when(replacementStrategy)
            .decide(eq(patron.getMemoryProps()), anyList());

        assertThat("Decisions strategy should be replaced", patron.shouldReplaceStrategy());
        verify(replacementStrategy).decide(eq(patron.getMemoryProps()), anyList());;
    }

    @Test
    public void record_doesNothing_whenHistoryIsEmpty() {
        testPatron.record(bar);

        assertThat("Patron history is empty", testPatron.getHistory(), is(ImmutableList.of()));
    }

    @Test
    public void record_updatesLastHistoricalEvent_whenHistoryIsNotEmpty_andLastDecisionIsTrue_withOvercrowdedBar() {
        doReturn(true)
            .when(bar)
            .isOvercrowded();
        final PatronHistoryEvent lastHistoryEvent =
            PatronHistoryEvent.builder()
                .decision(true)
                .strategy("Active")
                .build();

        testPatron.getHistory().add(lastHistoryEvent);

        testPatron.record(bar);

        final PatronHistoryEvent expectedHistoryEvent = PatronHistoryEvent.builder()
            .decision(lastHistoryEvent.getDecision())
            .strategy(lastHistoryEvent.getStrategy())
            .overcrowded(true)
            .correct(false)
            .build();

        assertThat("Patron history is updated with crowded status and false correct decision status",
            testPatron.getHistory(),
            is(ImmutableList.of(expectedHistoryEvent)));
    }

    @Test
    public void record_updatesLastHistoricalEvent_whenHistoryIsNotEmpty_andLastDecisionIsTrue_withoutOvercrowdedBar() {
        doReturn(false)
            .when(bar)
            .isOvercrowded();
        final PatronHistoryEvent lastHistoryEvent =
            PatronHistoryEvent.builder()
                .decision(true)
                .strategy("Active")
                .build();

        testPatron.getHistory().add(lastHistoryEvent);

        testPatron.record(bar);
        assertThat("Test patron history has one event", testPatron.getHistory().size(), is(1));

        final PatronHistoryEvent expectedHistoryEvent = PatronHistoryEvent.builder()
            .decision(lastHistoryEvent.getDecision())
            .strategy(lastHistoryEvent.getStrategy())
            .overcrowded(false)
            .correct(true)
            .build();

        assertThat("Patron history is updated with not crowded status and true correct decision status",
            testPatron.getHistory(),
            is(ImmutableList.of(expectedHistoryEvent)));
    }

    @Test
    public void record_updatesLastHistoricalEvent_whenHistoryIsNotEmpty_andLastDecisionIsFalse_withOvercrowdedBar() {
        doReturn(true)
            .when(bar)
            .isOvercrowded();
        final PatronHistoryEvent lastHistoryEvent =
            PatronHistoryEvent.builder()
                .decision(false)
                .strategy("Active")
                .build();

        testPatron.getHistory().add(lastHistoryEvent);

        testPatron.record(bar);

        final PatronHistoryEvent expectedHistoryEvent = PatronHistoryEvent.builder()
            .decision(lastHistoryEvent.getDecision())
            .strategy(lastHistoryEvent.getStrategy())
            .overcrowded(true)
            .correct(true)
            .build();

        assertThat("Patron history is updated with crowded status and true correct decision status",
            testPatron.getHistory(),
            is(ImmutableList.of(expectedHistoryEvent)));
    }

    @Test
    public void record_updatesLastHistoricalEvent_whenHistoryIsNotEmpty_andLastDecisionIsFalse_withoutOvercrowdedBar() {
        doReturn(false)
            .when(bar)
            .isOvercrowded();
        final PatronHistoryEvent lastHistoryEvent =
            PatronHistoryEvent.builder()
                .decision(false)
                .strategy("Active")
                .build();

        testPatron.getHistory().add(lastHistoryEvent);

        testPatron.record(bar);

        final PatronHistoryEvent expectedHistoryEvent = PatronHistoryEvent.builder()
            .decision(lastHistoryEvent.getDecision())
            .strategy(lastHistoryEvent.getStrategy())
            .overcrowded(false)
            .correct(false)
            .build();

        assertThat("Patron history is updated with not crowded status and false correct decision status",
            testPatron.getHistory(),
            is(ImmutableList.of(expectedHistoryEvent)));
    }

    @Test
    public void generateReport_returnsData() {
        final PatronReport expectedPatronReport = PatronReport.builder()
            .id(testPatron.getId())
            .history(testPatron.getHistory())
            .memoryProps(testPatron.getMemoryProps())
            .patience(testPatron.getPatience())
            .lastStrategySwitchStep(testPatron.getLastStrategySwitchStep())
            .build();

        assertThat("Report contains Patron state data", testPatron.generateReport(), is(expectedPatronReport));
    }
}
