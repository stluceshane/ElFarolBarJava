package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.agents.patron.PatronMemoryProps;
import abm.elfarolbar.strategies.decision.MinAverageAttendanceDecisionStrategy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MinAverageAttendanceDecisionStrategyTest {
    @Mock
    private Bar bar;

    private final PatronMemoryProps memoryProps = PatronMemoryProps.builder().build();

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder().build();
        doReturn(100)
            .when(bar)
            .getTotalPopulation();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar, memoryProps)));

        assertThat("Can return true with correct proportions", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(700.0, 75.0));
        assertThat("Can return false with correct proportions", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(300.0, 75.0));
    }

    @Test
    public void decide_returnsTrue_whenAverageHistoryIsAboveThreshold() {
        final int minAverage = 50;
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder()
            .minAverage(minAverage)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(minAverage, 101))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsTrue_whenAverageHistoryIsAtThreshold() {
        final int minAverage = 50;
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder()
            .minAverage(minAverage)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> minAverage)
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar, memoryProps));
    }

    @Test
    public void decide_returnsFalse_whenAverageHistoryIsBelowThreshold() {
        final int minAverage = 50;
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder()
            .minAverage(minAverage)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(0, minAverage + 1))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns false", !strategy.decide(bar, memoryProps));
    }

    @Test
    public void default_setsThresholdTo30() {
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder().build();
        final MinAverageAttendanceDecisionStrategy expectedStrategy = MinAverageAttendanceDecisionStrategy.builder()
            .minAverage(30)
            .build();


        assertThat("Default threshold is 30", strategy, samePropertyValuesAs(expectedStrategy));
    }

    @Test
    public void getName_returnsCorrectName() {
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("MinAverage-30"));
    }

    @Test
    public void getName_returnsCorrectName_whenThresholdIsNotDefault() {
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder()
            .minAverage(50)
            .build();

        assertThat("Returns correct name", strategy.getName(), is("MinAverage-50"));
    }

    @Test
    public void clone_createsCopy() {
        final MinAverageAttendanceDecisionStrategy strategy = MinAverageAttendanceDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
