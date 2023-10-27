package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PluralityThresholdAttendanceDecisionStrategyTest {
    @Mock
    private Bar bar;

    private final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();

    @Test
    public void decide_returnsTrue_whenOnlyAttendanceMatchesThreshold() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder().build();
        doReturn(ImmutableList.of(strategy.getThreshold()))
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns true if only bar attendance is at threshold", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsFalse_whenOnlyAttendanceMatchesThreshold_andStrategyIsReversed() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder()
            .reversed(true)
            .build();
        doReturn(ImmutableList.of(strategy.getThreshold()))
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns false if only bar attendance is at threshold with reversed strategy", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenNoHistory() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder().build();
        doReturn(ImmutableList.of())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns true if bar has no history", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsFalse_whenNoHistory_andStrategyIsReversed() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder()
            .reversed(true)
            .build();
        doReturn(ImmutableList.of())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns false if bar has no history with reversed strategy", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsFalse_whenRecentHistory_isAtOrBelowThreshold() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder().build();
        doReturn(
            new ImmutableList.Builder<Integer>()
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(strategy.getThreshold() + 1, 101))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(0, strategy.getThreshold() + 1))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .build()
        )
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns false if recent history is at or below threshold", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenRecentHistory_isAtOrBelowThreshold_andStrategyIsReversed() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder()
            .reversed(true)
            .build();
        doReturn(
            new ImmutableList.Builder<Integer>()
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(strategy.getThreshold() + 1, 101))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(0, strategy.getThreshold() + 1))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .build()
        )
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns true if recent history is at or below threshold with reversed strategy", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenRecentHistory_isAtOrAboveThreshold() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder().build();
        doReturn(
            new ImmutableList.Builder<Integer>()
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(0, strategy.getThreshold() + 1))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(strategy.getThreshold(), 101))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .build()
        )
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns true if recent history is at or above threshold", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsFalse_whenRecentHistory_isAtOrAboveThreshold_andStrategyIsReversed() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder()
            .reversed(true)
            .build();
        doReturn(
            new ImmutableList.Builder<Integer>()
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(0, strategy.getThreshold() + 1))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .addAll(
                    IntStream.range(0, memoryProps.getMemoryLength())
                        .map(idx -> RandomUtils.nextInt(strategy.getThreshold(), 101))
                        .boxed()
                        .collect(Collectors.toList())
                )
                .build()
        )
            .when(bar)
            .getAttendanceHistory();

        assertThat("Returns false if recent history is at or below threshold with reversed strategy", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void getName_returnsCorrectName() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("PluralityThreshold-30-false"));
    }

    @Test
    public void getName_returnsCorrectName_whenModified() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder()
            .threshold(70)
            .reversed(true)
            .build();

        assertThat("Returns correct name", strategy.getName(), is("PluralityThreshold-70-true"));
    }

    @Test
    public void clone_createsCopy() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }

    @Test
    public void clone_createsCopy_whenOriginalIsNotDefault() {
        final PluralityThresholdAttendanceDecisionStrategy strategy = PluralityThresholdAttendanceDecisionStrategy.builder()
            .threshold(70)
            .reversed(true)
            .build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
