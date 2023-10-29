package abm.elfarolbar.strategies.replacement;

import abm.elfarolbar.agents.patron.PatronHistoryEvent;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class FlatToleranceReplacementStrategyTest {

    @Test
    public void decide_returnsFalse_whenHistoryIsEmpty() {
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

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
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

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
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

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
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

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
    public void decide_returnsTrueAtAboutDefaultConfiguredFrequency_whenMostRecentDecisionsAreIncorrect() {
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder().build();

        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
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

        assertThat("Returns true decision when most recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(100.0, 75.0));
        assertThat("Returns false decision when most recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(900.0, 75.0));
    }

    @Test
    public void decide_returnsTrueAtAboutDefaultConfiguredFrequency_whenHalfOfRecentDecisionsAreIncorrect() {
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

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

        assertThat("Returns true decision when half of recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(100.0, 75.0));
        assertThat("Returns false decision when half of recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(900.0, 75.0));
    }

    @Test
    public void decide_returnsTrueAtAboutModifiedConfiguredFrequency_whenMostRecentDecisionsAreIncorrect() {
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

        final PatronMemoryProps props = PatronMemoryProps.builder()
            .failureTolerance(0.4f)
            .build();

        final List<PatronHistoryEvent> history = new ImmutableList.Builder<PatronHistoryEvent>()
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

        assertThat("Returns true decision when most recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(400.0, 75.0));
        assertThat("Returns false decision when most recent decisions are incorrect", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(600.0, 75.0));
    }

    @Test
    public void getName_returnsCorrectName() {
        final FlatToleranceReplacementStrategy strategy = FlatToleranceReplacementStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("FlatTolerance"));
    }

    private static PatronHistoryEvent createAgentHistoryEvent(final boolean correct) {
        return PatronHistoryEvent.builder()
            .strategy("test-strategy")
            .strategyNameToDecisionMap(ImmutableMap.of())
            .decision(Boolean.FALSE)
            .correct(correct)
            .build();
    }
}
