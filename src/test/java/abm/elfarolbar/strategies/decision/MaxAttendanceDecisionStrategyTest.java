package abm.elfarolbar.strategies.decision;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.doReturn;

import abm.elfarolbar.actors.bars.Bar;
import abm.elfarolbar.strategies.decision.MaxAttendanceDecisionStrategy;
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
public class MaxAttendanceDecisionStrategyTest {
    @Mock
    private Bar bar;

    @Test
    public void decide_returnsTrueOrFalse_whenNoHistory() {
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder().build();
        doReturn(100)
            .when(bar)
            .getTotalPopulation();

        final Map<Boolean, List<Integer>> mapResults = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.groupingBy(idx -> strategy.decide(bar)));

        assertThat("Can return true with correct proportions", Double.valueOf(mapResults.get(Boolean.TRUE).size()), closeTo(400.0, 75.0));
        assertThat("Can return false with correct proportions", Double.valueOf(mapResults.get(Boolean.FALSE).size()), closeTo(600.0, 75.0));
    }

    @Test
    public void decide_returnsTrue_whenMaxInHistoryIsAboveThreshold() {
        final int localMax = 60;
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder()
            .localMax(localMax)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(localMax, 101))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar), is(true));
    }

    @Test
    public void decide_returnsTrue_whenMaxInHistoryIsAtThreshold() {
        final int localMax = 60;
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder()
            .localMax(localMax)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> localMax)
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns true", strategy.decide(bar), is(true));
    }

    @Test
    public void decide_returnsFalse_whenMaxInHistoryIsBelowThreshold() {
        final int localMax = 60;
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder()
            .localMax(localMax)
            .build();

        doReturn(
            IntStream.range(0, RandomUtils.nextInt(1, 20))
                .map(idx -> RandomUtils.nextInt(0, localMax + 1))
                .boxed()
                .toList())
            .when(bar)
            .getAttendanceHistory();

        assertThat("Strategy returns false", strategy.decide(bar), is(false));
    }

    @Test
    public void default_setsThresholdTo60() {
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder().build();
        final MaxAttendanceDecisionStrategy expectedStrategy = MaxAttendanceDecisionStrategy.builder()
            .localMax(60)
            .build();


        assertThat("Default threshold is 60", strategy, samePropertyValuesAs(expectedStrategy));
    }

    @Test
    public void getName_returnsCorrectName() {
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder().build();

        assertThat("Returns correct name", strategy.getName(), is("Max-60"));
    }

    @Test
    public void getName_returnsCorrectName_whenThresholdIsNotDefault() {
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder()
            .localMax(50)
            .build();

        assertThat("Returns correct name", strategy.getName(), is("Max-50"));
    }

    @Test
    public void clone_createsCopy() {
        final MaxAttendanceDecisionStrategy strategy = MaxAttendanceDecisionStrategy.builder().build();

        assertThat("Clone is different object", strategy.clone(), not(strategy));
        assertThat("Clone has same properties as original", strategy.clone(), samePropertyValuesAs(strategy));
    }
}
