package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.strategies.decision.MaxAverageAttendanceDecisionStrategy;
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
public class MaxAverageAttendanceDecisionStrategyTest {
    @Mock
    private Bar bar;

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder().build();
        doReturn(100)
            .when(bar)
            .getTotalPopulation();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar)));

        assertThat("Can return true with correct proportions", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(800.0, 75.0));
        assertThat("Can return false with correct proportions", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(200.0, 75.0));
    }

    @Test
    public void decide_returnsFalse_whenAverageHistoryIsAboveThreshold() {
        final int maxAverage = 80;
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder()
            .maxAverage(maxAverage)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(maxAverage, 101))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns false", strategy.decide(bar), is(false));
    }

    @Test
    public void decide_returnsTrue_whenAverageHistoryIsAtThreshold() {
        final int maxAverage = 80;
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder()
            .maxAverage(maxAverage)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> maxAverage)
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar), is(true));
    }

    @Test
    public void decide_returnsTrue_whenAverageHistoryIsBelowThreshold() {
        final int maxAverage = 80;
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder()
            .maxAverage(maxAverage)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(0, maxAverage + 1))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar), is(true));
    }

    @Test
    public void default_setsThresholdTo80() {
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder().build();
        final MaxAverageAttendanceDecisionStrategy expectedStrategy = MaxAverageAttendanceDecisionStrategy.builder()
            .maxAverage(80)
            .build();


        assertThat("Default threshold is 80", strategy, samePropertyValuesAs(expectedStrategy));
    }

    @Test
    public void getName_returnsCorrectName() {
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("MaxAverage-80"));
    }

    @Test
    public void getName_returnsCorrectName_whenThresholdIsNotDefault() {
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder()
            .maxAverage(50)
            .build();

        assertThat("Returns correct name", strategy.getName(), is("MaxAverage-50"));
    }

    @Test
    public void clone_createsCopy() {
        final MaxAverageAttendanceDecisionStrategy strategy = MaxAverageAttendanceDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
