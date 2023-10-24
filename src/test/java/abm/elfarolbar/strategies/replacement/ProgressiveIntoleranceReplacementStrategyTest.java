package abm.elfarolbar.strategies.replacement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import abm.elfarolbar.agents.patron.PatronHistoryEvent;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProgressiveIntoleranceReplacementStrategyTest {

    @Test
    public void decide_returnsFalse_whenHistoryIsEmpty() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();
        final List<PatronHistoryEvent> history = ImmutableList.of();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Never returns true decision when there is no history", mapResults.containsKey(Boolean.TRUE), is(false));
        assertThat("Returns false decision when there is no history", mapResults.get(Boolean.FALSE).size(), is(1000));
    }

    @Test
    public void decide_returnsFalse_whenRecentDecisionIsCorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();

        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
            .addAll(
                IntStream.range(0, 100)
                    .boxed()
                    .map(idx -> createAgentHistoryEvent(false))
                    .collect(Collectors.toList())
            )
            .add(
                createAgentHistoryEvent(true)
            )
            .build();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Never returns true decision when most recent decision is correct", mapResults.containsKey(Boolean.TRUE), is(false));
        assertThat("Returns false decision when most recent decision is correct", mapResults.get(Boolean.FALSE).size(), is(1000));
    }

    @Test
    public void decide_returnsFalse_whenOnlyDecisionIsCorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();
        final List<PatronHistoryEvent> history = ImmutableList.of(
            createAgentHistoryEvent(true)
        );

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Never returns true decision when only decision is correct", mapResults.containsKey(Boolean.TRUE), is(false));
        assertThat("Returns false decision when only decision is correct", mapResults.get(Boolean.FALSE).size(), is(1000));
    }

    @Test
    public void decide_returnsFalse_whenMostRecentDecisionsAreCorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();

        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
            .addAll(
                IntStream.range(0, 100)
                    .boxed()
                    .map(idx ->
                        createAgentHistoryEvent(true)
                    )
                    .collect(Collectors.toList())
            )
            .add(
                createAgentHistoryEvent(false)
            )
            .build();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Never returns true decision when most recent decisions are correct", mapResults.containsKey(Boolean.TRUE), is(false));
        assertThat("Returns false decision when most recent decisions are correct", mapResults.get(Boolean.FALSE).size(), is(1000));
    }

    @Test
    public void decide_returnsTrueAtAboutDefaultConfiguredFrequency_whenOnlyRecentDecisionAreIncorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();

        final List<PatronHistoryEvent> history = ImmutableList.of(
            createAgentHistoryEvent(false)
        );

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Returns true decision when most recent decision is incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(100.0, 75.0));
        assertThat("Returns false decision when most recent decision is incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(900.0, 75.0));
    }

    @Test
    public void decide_returnsTrueAtAboutModifiedConfiguredFrequency_whenOnlyRecentDecisionIsIncorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder()
            .failureTolerance(0.3f)
            .build();

        final List<PatronHistoryEvent> history = ImmutableList.of(
            createAgentHistoryEvent(false)
        );

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Returns true decision when most recent decision is incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(300.0, 75.0));
        assertThat("Returns false decision when most recent decision is incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(700.0, 75.0));
    }

    @Test
    public void decide_returnsTrueAtExpectedFrequency_whenMostRecentDecisionsAreIncorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();

        final int numberFailedDecisions = 3;

        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
            .addAll(
                IntStream.range(0, numberFailedDecisions)
                    .boxed()
                    .map(idx -> createAgentHistoryEvent(false))
                    .collect(Collectors.toList())
            )
            .build();

        final int totalTestDecisions = 1000;
        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, totalTestDecisions)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        final double expectedTrueDecisions = numberFailedDecisions * 100.0;
        assertThat("Returns true decision when most recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(expectedTrueDecisions, 75.0));

        final double expectedFalseDecisions = totalTestDecisions - expectedTrueDecisions;
        assertThat("Returns false decision when most recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(expectedFalseDecisions, 75.0));
    }

    @Test
    public void decide_returnsTrueAtExpectedFrequency_whenHalfOfRecentDecisionsAreIncorrect() {
        final ProgressiveIntoleranceReplacementStrategy strategy = ProgressiveIntoleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();

        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
            .addAll(
                IntStream.range(0, 5)
                    .boxed()
                    .map(idx ->
                        createAgentHistoryEvent(true)
                    )
                    .collect(Collectors.toList())
            )
            .addAll(
                IntStream.range(0, 5)
                    .boxed()
                    .map(idx -> createAgentHistoryEvent(false))
                    .collect(Collectors.toList())
            )
            .build();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(props, history)));

        assertThat("Returns true decision when half of recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(500.0, 75.0));
        assertThat("Returns false decision when half of recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(500.0, 75.0));
    }

    private static PatronHistoryEvent createAgentHistoryEvent(final boolean correct) {
        return PatronHistoryEvent.builder()
            .strategy("test-strategy")
            .decisions(ImmutableMap.of())
            .decision(Boolean.FALSE)
            .correct(correct)
            .build();
    }
}
